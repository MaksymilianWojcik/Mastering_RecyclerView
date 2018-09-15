package com.example.mwojcik.recyclerviewone;

import android.support.annotation.NonNull;
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
     */
    @NonNull
    @Override
    public RecyclerViewAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateviewHolder call, where parent is: " + parent.getClass().getName().toString());

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_item, parent, false);

        MyViewHolder viewHolder = new MyViewHolder(view);

        return viewHolder;
    }

    /***
     * Ustawia view attributes w oparciu o dane (data)
     *
     * Wywolywane tyle razy ile mamy itemow. Wywolywane juz po onCreateViewHolder i utworzeniu ViewHoldera, czyli także
     * wywołaniu konstruktora tego ViewHodldera. Metoda wywolywana jest dla kazdego itemu.
     */
    @Override
    public void onBindViewHolder(@NonNull RecyclerViewAdapter.MyViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder call for position: " + position);

        Model model = dataList.get(position);
        holder.titleTextView.setText(model.getTitle());
        holder.descriptionTextView.setText(model.getDescription());
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
     * W celu dodania listenra w activity wyzej np czy fragmencie.
     */
    private RecyclerViewOnItemClickListener listener;
    public interface RecyclerViewOnItemClickListener {
        void onItemClick(View itemView, int position);
    }
    public void setOnItemClickListener(RecyclerViewOnItemClickListener  listener){
        this.listener = listener;
    }

    /***
     * Zapewnia bezpośrednią referencje do każdego z views w itemie. Używane do cachowania widoków wewnątrz layoutu
     * itema dla szybkiego dostępu.
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
             */
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION){
                        Model model = dataList.get(position);
                        Toast.makeText(view.getContext(), model.getTitle() + " clicked", Toast.LENGTH_SHORT).show();
                        //customowy listener
                        listener.onItemClick(itemView, position);
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
