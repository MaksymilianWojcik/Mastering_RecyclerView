package com.example.mwojcik.recyclerviewone;

import android.support.annotation.NonNull;
import android.support.v7.recyclerview.extensions.AsyncListDiffer;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

/***
 * RecyclerView jest to ViewGroup zastpęujący ListView i GridView dostępny w support-v7. Powinniśmy używać go kiedy tylko
 * posiadamy kolekcje danych ktorej elementy mogą się zmieniać podczas runtimeu na podstawie akcji użytkownika lub eventów
 * sieciowych.
 *
 * Zeby używać RecyclverView musimy poznać pracę z:
 * - RecyclerView.Adapter - do obsługi kolekcji danych i powiązaniach ich do widoku (view).
 * - LayoutManager - Pomaga pozycjonować itemy (np. horyzontalnie)
 * - ItemAnimator - Pomaga z animacją itemów dla powszechnych operacji jak np. dodawanie czy odejmowanie.
 *
 * Przeciwnie do ListView tutaj ViewHolder jest wymagany w Adapterach. W listView adapterach nie były one wymagane, chociaż
 * zwiększają perforemance. W RecyclerView są one wymagane i używamy do tego RecyclerView.Adapter.
 * Co jeszcze warte uwagi, ListView mają adaptery dla rożnych źródeł danych (np. ArrayAdapter czy CursorAdapter). RecyclerView
 * natomiast wymaga customowej implementacji żeby wspierać takie dane w adapterze.
 *
 *
 * RecyclerViewAdapter
 * Służy do populacji danych do RecyclerView. Jego rolą jest po prostu konwertowanie obiektu na danej pozycji do wstawienia
 * w row_item. W RecyclerView adapter wymaga obiektu ViewHoldera, który opisuje i dostarcza dostęp do wszystkich widoków w każdym
 * row_itemie.
 *
 *
 * ViewHolder to taki wzorzec, w ktorym mamy obiekt który zawiera View i Dane do zrenderowania na tym View. Definiujemy
 * ja zazwyczaj jako klasy prywatne wewnatrz adaptera.
 *
 *
 * Pare uwag:
 * - Nie wykonywac animacji na view wewnatrz viewholdera (np. itemView.animate(). ItemAnimator jest jedynym komponentem
 * ktory moze animowac viewsy.
 * - Nie uzywac notifyItemRangeChanged w ten sposob: notifyItemRangeChanged(0, getItemsCount())
 * - Do zarządzania updejtami adaptera uzywac DiffUtil - obsłuży on wsyzstkie kalkulacje zmian i rozdzieli je do adaptera
 * - Nigdy nie ustawiac View.OnCliCklistener wewnątrz onBindViewHodler! Zrobic osobno clicklistenra i ustawic go w konstruktorze
 * viewholdera (najlepiej ustawic i odwolac sie do listenera, ale mozemy tez tam po prostu go zrobic)
 * - Uzywac setHasStableIds(true) z getItemId(int position) a RecyclerView automatycznie obsłuży wszystkie animacje na prostym wywołaniu
 * notifyDataSetChanged().
 * Jeżeli chcemy smoothscrolling, nie możemy o tym zapomnieć:
 * - mamy tylko 16ms do wykonania calej pracy/per framme
 * - item layout powinien być prosty
 * - unikać deep layout hierarchii
 * - unikać overdraw issue,
 * - nie ustawiac zbyt długich textów w TextView, bo text line wrap są ciężkimi kalkulacjami. Usatwić max lines z text i ellipsis
 * - używać LayoutManager.setItemPrefetchEnabled() dla zagnieżdżonych RecyclerViews dla lepszego performancu renderingu
 *
 *
 * LayoutManager - dołącza, mierzy/oblicza wszystkie child views RecyclerView w czasie rzeczywistym. Jak user scrolluje widok,
 * to LayoutManager określa kiedy nowy child view zostanie dodany i kiedy starty child view zostanie odłączony (detached) i usunięty.
 * Możemy stworzyć customowy LayoutManager rozrzeszając RecyclerView.LayoutManager lub np. inne implementacje LayoutManagera:
 * LinearyLayoutManager, GridLayoutManager, StaggeredGridLayoutManager.
 *
 *
 *
 *
 * RecyclerView.ItemAnimator - klasa która określa wykonywane na itemach animacje i będzie animować zmiany ViewGropud jak np.
 * dodawanie, usuwanie, zaznaczenie wykonywane/inforowane na adapterze. DefaultItemAnimator jest bazową animacją dostępną
 * domyślnie w RecyclerView. Żeby skustomizować DefaultItemAnimator wystarczy dodać item animator do RecyclerView:
 * RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
 * itemAnimator.setAddDuration(1000);
 * itemAnimator.setRemoveDuration(1000);
 * recyclerView.setItemAnimator(itemAnimator);
 * Przyklad pokazany w klasie MainActivity
 * INNYM SPOSOBEM ANIMOWANIA RECYCLERVIEW ITEMOW jest wykorzystanie Androidowych Interpolatorów. Interpolator definiuje
 * częśtość zmiany aniumacji. Przykład xmlowy reprezentujący dwie animacje z wykorzystaniem interpolatorów. Dodaje się je
 * do res/anim/:
 *
 * overshoot.xml
 * <?xml version="1.0" encoding="utf-8"?>
 * <set xmlns:android="http://schemas.android.com/apk/res/android"
 * android:interpolator="@android:anim/anticipate_overshoot_interpolator">
 * <translate
 * android:fromYDelta="-50%p"
 * android:toYDelta="0"
 * android:duration="2000"
 * />
 * </set>
 *
 * bounce.xml
 *
 *<set xmlns:android="http://schemas.android.com/apk/res/android"
 * android:interpolator="@android:anim/bounce_interpolator">
 * <translate
 * android:duration="1500"
 * android:fromYDelta="-150%p"
 * android:toYDelta="0"
 * />
 * </set>
 *
 * A w Adapterze RecyclerView trezba dodac funkcje:
 *
 * public void animate(RecyclerView.ViewHolder viewHolder) {
 * final Animation animAnticipateOvershoot = AnimationUtils.loadAnimation(context, R.anim.bounce_interpolator);
 * viewHolder.itemView.setAnimation(animAnticipateOvershoot);
 * }
 *
 * Te animacje co prawda męczą oczy. Metode te wywolujemy wewnatrz onBindViewHolder, bo tam powinno się to odbywać. To
 * jako taka dodatkowa informacja
 *
 *
 *
 */
public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> {

    private static String TAG = "RecyclerViewAdapter";
    private static String TAG_VH = "RecyclerViewAdapterVH";

    List<Model> dataList;

    public RecyclerViewAdapter(List<Model> dataList) {
        Log.d(TAG, "constructor call");
        this.dataList = dataList;
    }


    /***
     * Inflatuje item layout i tworzy holder
     *
     * Wywolywane tyle razy ile mamy itemow jako pierwsza metoda, jeszcze przed wywołaniem konstruktora ViewHoldera,
     * czyli przed utworzeniem takiego obiektu. Jest to jasne, bo przeciez tworzymy go wewnatrz tej metody. Wywoływane
     * jest tylko wtedy, kiedy naprawdę musimy utworzyć nowy view.
     *
     * Inflatuje row layout i inicjalizuje ViewHolder. Jak już ViewHolder jest zainicjalizowany to zarządza ten viewholder finViewById do
     * bindowania widoków i recyclowania ich by uiknąć potwarzanych wywołań
     */
    @NonNull
    @Override
    public RecyclerViewAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateviewHolder call, where parent is: " + parent.getClass().getName().toString());

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_item, parent, false);

        MyViewHolder viewHolder = new MyViewHolder(view);

        return viewHolder;
    }

//    //do selekcji itema
//    int selectedPostion = RecyclerView.NO_POSITION;

    /***
     * Ustawia view attributes w oparciu o dane (data)
     *
     * Wywolywane tyle razy ile mamy itemow. Wywolywane juz po onCreateViewHolder i utworzeniu ViewHoldera, czyli także
     * wywołaniu konstruktora tego ViewHodldera. Metoda wywolywana jest dla kazdego itemu.
     * Wykorzystuje ViewHolder skonstruowany w onCreateViewHolder do wypełnienia danego rowa RecyclerView danymi
     */
    @Override
    public void onBindViewHolder(@NonNull RecyclerViewAdapter.MyViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder call for position: " + position);

        Model model = dataList.get(position);
        holder.titleTextView.setText(model.getTitle());
        holder.descriptionTextView.setText(model.getDescription());

        /***
         * Możemy np. ustawić taga na dany item żeby dostać go w np. onClick listenerze, dodałem jako przykład.
         * Ustawiamy to na itemView holdera, czyli dla danego row itema.
         */
        holder.itemView.setTag(model);

        //Do zaznaczenia wybranego itema
//        holder.itemView.setSelected(selectedPostion == position);
    }

    /***
     *
     * Określa liczbę itemów
     */
    @Override
    public int getItemCount() {
        //Log.d(TAG, "getItemCount call");
        return dataList.size();
    }


    /***
     * Customowy listener w celu dodania listenra w activity wyzej np czy fragmencie.
     */
    private RecyclerViewOnItemClickListener listener;
    public interface RecyclerViewOnItemClickListener {
        void onItemClick(View itemView, int position);
    }
    public void setOnItemClickListener(RecyclerViewOnItemClickListener  listener){
        this.listener = listener;
    }



    /***
     * Przyklad uzycia klasy diffUtil
     */
    public void diffUtilTest(List<Model> modelList){
        RecyclerViewDiffUtilCallback callback = new RecyclerViewDiffUtilCallback(this.dataList, modelList);
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(callback);

        this.dataList.clear();
        this.dataList.addAll(modelList);
        result.dispatchUpdatesTo(this);
    }

    /***
     * Przyklad uzycia klasy diffUtil do sortowania
     */
    public void updateSortedList(List<Model> newList){
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new RecyclerViewDiffUtilCallback(this.dataList, newList));
        this.dataList.clear();
        this.dataList.addAll(newList);
        diffResult.dispatchUpdatesTo(this);
    }

    /***
     *
     */
    public void updateSortedListWithAsyncDiff(List<Model> newList){
        //Kaklukacje powinny się odbywać w backgroundtHreadize i do teog wykorzystuje się
        //AsyncListDIffer: https://developer.android.com/reference/android/support/v7/recyclerview/extensions/AsyncListDiffer

    }

    /***
     * Zapewnia bezpośrednią referencje do każdego z views w itemie. Używane do cachowania widoków wewnątrz layoutu
     * itema dla szybkiego dostępu.
     * RecyclerView wykorzystuje ViewHolder do przechowywania referencji do odpowiednich widoków dla każdego entry w RecyclerView.
     * Pozwala to uniknąć wywołań wszystkich finViewById metod w adapterze do wyszukania widoków do wypełnienia danymi.
     */
    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView titleTextView;
        TextView descriptionTextView;

        /***
         * Konstruktor akceptuje cały item row i wykonuje wyszukiwanie widoku by znalexć każdy subview
         */
        public MyViewHolder(final View itemView) {

            /***
             * Przechowuje itemView w publicznej finalnej zmiennej która może być używana do uzyskania dostępu do kontekstu
             * z dowolnegj instancji ViewHoldera
             */
            super(itemView);

            Log.d(TAG_VH, "constructor call");
            titleTextView = (TextView) itemView.findViewById(R.id.recyclerview_item_title);
            descriptionTextView = (TextView) itemView.findViewById(R.id.recyclerview_item_description);

            /***
             * W przeciwienstwie do ListView, recyclerView nie ma specjlanych przepisów dotyczących dołączania click handlerów
             * do itemów, jak np. w ListView metoda setOnItemClickListener. Aby jednak osiągnąć podobny efekt możemy dołączyć
             * click event wewnątrz ViewHoldera w adapterze. Tak to się powinno robić. Jest jeszcze przypadek że np. chcielibyśmy
             * stworzyć takiego click handlera dla danego itema ale w np. activity lub w fragmncie w którym zawarty jest ten recycler view.
             * W takim wypadku musimy stworzyć customowego listenera (interefjs) w adapterze i wystrzeliwać eventy do implementacji
             * tego listenera (interfejsu) w danym activity / fragmencie. Jest to tu pokazane
             *
             * Ciekawe podejście do zrobienia właśnego itemClickListenera podobnego do tego w listview: https://www.sitepoint.com/mastering-complex-lists-with-the-android-recyclerview/
             */
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION){
                        /***
                         * I teraz mamy 2 sposoby na uzyskanie obiektu, albo przez pobranie pozycji itema z listy,
                         * albo tak jak wyżej dodałem przez taga:
                         */
//                        Model model = dataList.get(position);
//                        Model model = (Model) itemView.getTag();
                        //Moze byc view, bo przxeciez danym view jest rownie dobrze itemview holdera - bo to ten sam row item.
                        Model model = (Model) view.getTag();
                        Toast.makeText(view.getContext(), model.getTitle() + " clicked", Toast.LENGTH_SHORT).show();

                        //poinformowanie customowego listenera o evencie do odebrania w MainActivity
                        listener.onItemClick(itemView, position);

                        //do zaznaczenia kliknietego itema
                        //najpierw informujemy o zmianie stary item, a nastepnie nowy
//                        notifyItemChanged(selectedPostion);
//                        selectedPostion = position;
//                        notifyItemChanged(selectedPostion);
                    }
                }
            });
        }


    }
}

/*
Logi z listy z 4 elementami po wystartowaniu:
D/RecyclerViewAdapter: constructor call
D/RecyclerViewAdapter: getItemCount call
D/RecyclerViewAdapter: getItemCount call
D/RecyclerViewAdapter: getItemCount call

D/RecyclerViewAdapter: onCreateviewHolder call, where parent is: android.support.v7.widget.RecyclerView
D/RecyclerViewAdapterVH: constructor call
D/RecyclerViewAdapter: onBindViewHolder call for position: 0
D/RecyclerViewAdapter: getItemCount call

D/RecyclerViewAdapter: onCreateviewHolder call, where parent is: android.support.v7.widget.RecyclerView
D/RecyclerViewAdapterVH: constructor call
D/RecyclerViewAdapter: onBindViewHolder call for position: 1
D/RecyclerViewAdapter: getItemCount call

D/RecyclerViewAdapter: onCreateviewHolder call, where parent is: android.support.v7.widget.RecyclerView
D/RecyclerViewAdapterVH: constructor call
D/RecyclerViewAdapter: onBindViewHolder call for position: 2
D/RecyclerViewAdapter: getItemCount call

D/RecyclerViewAdapter: onCreateviewHolder call, where parent is: android.support.v7.widget.RecyclerView
D/RecyclerViewAdapterVH: constructor call
D/RecyclerViewAdapter: onBindViewHolder call for position: 3
D/RecyclerViewAdapter: getItemCount call

D/RecyclerViewAdapter: getItemCount call
D/RecyclerViewAdapter: getItemCount call
D/RecyclerViewAdapter: getItemCount call
D/RecyclerViewAdapter: getItemCount call
D/RecyclerViewAdapter: getItemCount call
D/RecyclerViewAdapter: getItemCount call
D/RecyclerViewAdapter: getItemCount call
D/RecyclerViewAdapter: getItemCount call
D/RecyclerViewAdapter: getItemCount call
D/RecyclerViewAdapter: getItemCount call
D/RecyclerViewAdapter: getItemCount call
D/RecyclerViewAdapter: getItemCount call
D/RecyclerViewAdapter: getItemCount call
D/RecyclerViewAdapter: getItemCount call
D/RecyclerViewAdapter: getItemCount call
D/RecyclerViewAdapter: getItemCount call
D/RecyclerViewAdapter: getItemCount call
D/RecyclerViewAdapter: getItemCount call
D/RecyclerViewAdapter: getItemCount call

Pozniej przy np. 10 itemach i przewijaniu w dol w gore wywoluje OnBindViewHodler tylko dla pozycji 0 i 10.
 */
