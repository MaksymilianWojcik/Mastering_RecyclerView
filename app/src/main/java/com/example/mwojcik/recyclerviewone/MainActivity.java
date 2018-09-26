package com.example.mwojcik.recyclerviewone;

import android.content.Intent;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.Explode;
import android.util.Log;
import android.view.View;
import android.view.Window;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    FloatingActionButton fab;

    /***
     * Zamockowane dane na potrzeby prezentacji recyclerView - normalnie pobieralibysmy to z internetu czy bazy danych.
     * Musimy je zrobic globalne dla klasy, zeby mozna bylo je w dowolych metodach zmieniac. Tak samo adapter,
     * zebysmy mogli go w dowolnym miejscu zmodyfikować (np. dodać itemy do listy)
     */
    List<Model> mockData;
    RecyclerViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
//        getWindow().setExitTransition(new Explode());
//        getWindow().setTransitionBackgroundFadeDuration(1000);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = (RecyclerView) findViewById(R.id.main_recycler_view);
        fab = (FloatingActionButton) findViewById(R.id.fab);

//        final List<Model> mockData = new ArrayList<>(); - po ustawieniu globalnie
        mockData = new ArrayList<>();
        mockData.add(new Model(2, "Zorro", "Opowiesc o zorro"));
        mockData.add(new Model(1, "Batman", "Opowiesc o batmanie"));
        mockData.add(new Model(5, "Spiderman", "Opowiesc o spidermanie"));
        mockData.add(new Model(7, "Wolverine", "Opowiesc o wolverinie"));
        mockData.add(new Model(3, "Silversurfer", "Opowiesc o silversurferze"));
        mockData.add(new Model(8, "Flash", "Opowiesc o flashu"));
        mockData.add(new Model(4, "Anakin Skywalker", "Opowiesc o darth vaderze"));
        mockData.add(new Model(6, "Superman", "Opowiesc o klarku kencie"));
        mockData.add(new Model(9, "Wonderwoman", "Opowiesc o wonderwoman"));
        mockData.add(new Model(10, "Magneto", "Opowiesc o magneto"));
        mockData.add(new Model(11, "Hulk", "Opowiesc o hulku"));

//        RecyclerViewAdapter adapter = new RecyclerViewAdapter(mockData); - po ustawieniu globalnej
        adapter = new RecyclerViewAdapter(mockData);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);

        /***
         * Ustawiamy sobie naszego customowego listenera do oderbania na adapterze
         */
        adapter.setOnItemClickListener(new RecyclerViewAdapter.RecyclerViewOnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {
                /***
                 * Dzięki temu, że ustawiliśmy sobie wcześniej TAG w viewHolderze, możemy go również
                 * użyć tutaj, bo przecież itemView jest cały czas ten sam - row item
                 */
//                Model model = mockData.get(position);
                Model model = (Model) itemView.getTag();
                Log.d("MainActivity", "Received itemClick from listener for item: " + model.getTitle());

                // mozemy np. usunac item po klikninięciu:
//                deleteItem(position);


                //przeniesienie do nowego activity. Moze byc tu lub np. w viewholderze
                Intent intent = new Intent(MainActivity.this, NewActivity.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(MainActivity.this, Pair.create(itemView.findViewById(R.id.recyclerview_item_title), "text1"),
                            Pair.create(itemView.findViewById(R.id.recyclerview_item_description), "text2"));
                    startActivity(intent, options.toBundle());
                } else {
                    startActivity(intent);
                }
            }
        });

        //Customowy itemDecoraton: https://gist.github.com/nesquena/db922669798eba3e3661
        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        MyDividerItemDecoration myDividerItemDecoration = new MyDividerItemDecoration(this, LinearLayoutManager.VERTICAL, 16);

        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        itemAnimator.setAddDuration(500);
        itemAnimator.setRemoveDuration(500);
//        itemAnimator.setMoveDuration(1000);
        //        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setItemAnimator(itemAnimator);

//        recyclerView.addItemDecoration(itemDecoration);
        recyclerView.addItemDecoration(myDividerItemDecoration);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        //Click listenery: https://gist.github.com/nesquena/231e356f372f214c4fe6

        /***
         * Listener kliknięcia floating action buttona. Co warte tutaj uwagi, jak dodamy floating action buttona (najlepiej w coordinator layoucie)
         * to jego kliknięcie nic nie da, nie zainicjalizuje zadnej animacji itp, a nawet kliknie się item z recyclerview, który jest pod\
         * danym fab buttonem - czyli button jest widoczny, ale tak jakby nie istniał.
         */
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("MainActivity", "Floating action button tapped");
                addNewItemToList();
//                Snackbar.make(view, "Floating button tapped", Snackbar.LENGTH_SHORT)
//                        .setAction("UNDO", new View.OnClickListener() {
//                            @Override
//                            public void onClick(View view) {
//
//                            }
//                        }).show();
            }
        });

    }


    /***
     * DOdajemy sobie nowy item do listy i powiadamiamy adapter o zmianie. Co tutaj jest warte uwagi, w przeciwienstwie do listview
     * w RecyclerView nie powinnismy naduzywać notifyDataSetChanged, tylko bardziej szczegółowe działania - a takie działania,
     * takie metody nam recyclerview udostępnia. Mamy tak:
     * notifyItemChanged(int position), notifyItemInserted(int position), notifyItemRemoved(int position), notifyDataSetChanged()).
     * notifyDataSetChanged() powinno być używane w konieczności, lepiej robić to szczegółowo - po to są te metody.
     *
     * Nizej pokazane przyklady. Ale jeszcze jedno ważne do powiedzenia tutaj - często możemy mieć bardziej kompleksowe zmiany,
     * bardziej skomplikowane, jak np. sortowanie - wtedy nie możemy powiedzieć tak łatwo który item row się zmienił, które elementy itp.
     * W takim wypadku zazwyczaj wywołalibyśmy notifyDataSetChanged() na całym adapterze, co jednak eliminuje możliwość
     * wykonania sekwencji animacji do pokazania co się zmieniło. Tutaj recyclerView od 27.1. support library udostępnił nowe rzeczy, takie
     * jak np. ListAdapter dla RecyclerView, klsa która ułatwia rozpoznanie czy item został wsatwione, usunięty, zupdejtowany itp.
     * Taki ListAdapter zbudowany jest na DiffUtil - my możemy więc też użyć tego DiffUtil, który został dodany wcześniej,
     * bo chyba w v24. Jest to klasa, który pomaga wyznaczyć/obliczyć różnice między starą a nową listą. Klasa ta używa takiego samego jak
     * ListAdapter algorytmy do obliczania tych zmian. Rekomendowane jest jednak dla większych list, żeby wykonywane te obliczenia były
     * w background threadzie. Zobaczymy jak taki DiffUtil użyć.Dla tego przykładu zrobiłem klasę ModelDiffUtil, która możemyt wywołać tutaj
     * lub najlpeiej - dla czystosci kodu - w adapterze, bo to z nim pracuje ta klasa.
     */
    private void addNewItemToList(){
        addNewItemWithInserted();
//        addNewItemWithDiffUtil();
//        sortItemsWithDiffUtil();
    }


    private void addNewItemWithDataSetChanged(){
        //Doda nam nowy item na dole listy - (zwrócmy uwage, że to nam nie scrolluje, tylko dodaje się nowy item a nasz scroll)
        mockData.add(new Model(13, "Nowy item nr. " + (mockData.size()+1), "opis"));
        adapter.notifyDataSetChanged();
    }

    private void addNewItemWithInserted(){
////        Doda nam nowy item na dole listy - (zwrócmy uwage, że to nam nie scrolluje, tylko dodaje się nowy item a nasz scroll)
//        mockData.add(mockData.size(), new Model(mockData.size()+1, "Nowy item nr. " + (mockData.size()+1), "opis"));
//        adapter.notifyItemInserted(mockData.size());
//        //Dodany scroll do nowej pozycji -1, dlatego ze indeksujemy przeciez od 0 a nie od 1 i po prostu probowalibysmy
//        //wskoczyc na miejsce nie istniejace - co mnei dziwi tutaj, ze nie leci zaden exception w takim wypadku?
//        recyclerView.scrollToPosition(mockData.size()-1);

        //Lub na początek: (zwrócmy uwage, że to nam nie scrolluje, tylko dodaje się nowy item a nasz scroll)
        //na liście jest dalej w tym samym miejscu
        mockData.add(0, new Model(mockData.size()+1, "Nowy item nr. " + (mockData.size()+1), "opis"));
        adapter.notifyItemInserted(0);
        recyclerView.scrollToPosition(0);
    }

    private void addNewItemWithRangeChanged(){
        //Lub jeszcze inaczej, jak chcemy dodać więcej niż 1 elementów to najlepiej tak:
        int curSize = adapter.getItemCount();

        ArrayList<Model> newItems = new ArrayList<>();
        for (int i = curSize; i < curSize+6; i++){
            newItems.add(new Model(i+1, "numer " + (i+1), "opis"));
        }
        mockData.addAll(newItems);
        adapter.notifyItemRangeChanged(curSize, newItems.size());
        recyclerView.scrollToPosition(curSize);
    }

    private void sortItemsWithDiffUtil(){
        List<Model> models = new ArrayList<>();
        models.addAll(mockData);

        Collections.sort(models, new Comparator<Model>() {
            @Override
            public int compare(Model model, Model t1) {
                return model.getId() - t1.getId();
            }
        });

        adapter.updateSortedList(models);
    }

    /***
     * Tutaj mamy przykład wywołania DiffUtil do zmiany listy. Uzywac jak chcemy wiecej elemtnow dodac
     */
    private void addNewItemWithDiffUtil(){
        int curSize = adapter.getItemCount();
        ArrayList<Model> newItems = new ArrayList<>();
        for (int i = curSize; i < curSize+6; i++){
            newItems.add(new Model(i+1, "numer " + (i+1), "opis"));
        }
        adapter.diffUtilTest(newItems);
    }

    private void deleteItem(int position){
        mockData.remove(position);
//        adapter.notifyDataSetChanged();
        adapter.notifyItemRemoved(position);

        // Albo jak byśmy przekazali obiekt Model jako parametr metody
//        int position = list.indexOf(model);
//        list.remove(position);
//        notifyItemRemoved(position);
    }

//    private void diffUtilTest(List<Model> modelList){
//        RecyclerViewDiffUtilCallback callback = new RecyclerViewDiffUtilCallback(this.mockData, modelList);
//        DiffUtil.DiffResult result = DiffUtil.calculateDiff(callback);
//
//        this.mockData.clear();
//        this.mockData.addAll(modelList);
//        result.dispatchUpdatesTo(adapter);
//    }

}



//    //Doda nam nowy item na dole listy - (zwrócmy uwage, że to nam nie scrolluje, tylko dodaje się nowy item a nasz scroll)
////        mockData.add(new Model(13, "Nowy item nr. " + (mockData.size()+1), "opis"));
//////        adapter.notifyDataSetChanged();
////        adapter.notifyItemInserted(mockData.size()+1);
//
//
//    //Lub na początek: (zwrócmy uwage, że to nam nie scrolluje, tylko dodaje się nowy item a nasz scroll)
//    //na liście jest dalej w tym samym miejscu
////        mockData.add(0, new Model(13, "Nowy item nr. " + (mockData.size()+1), "opis"));
////        adapter.notifyItemInserted(0);
//
//
//    //Lub jeszcze inaczej, jak chcemy dodać więcej niż 1 elementów to najlepiej tak:
//    int curSize = adapter.getItemCount();
//
//    ArrayList<Model> newItems = new ArrayList<>();
//        for (int i = curSize; i < curSize+6; i++){
//        newItems.add(new Model(i+1, "numer " + (i+1), "opis"));
//        }
//        //zakomentowane zeby sprawdic diffUtil.
////        mockData.addAll(newItems);
////        adapter.notifyItemRangeChanged(curSize, newItems.size());
//
//        /***
//         * Tutaj mamy przykład wywołania DiffUtil do zmiany listy.
//         */
//        adapter.diffUtilTest(newItems);