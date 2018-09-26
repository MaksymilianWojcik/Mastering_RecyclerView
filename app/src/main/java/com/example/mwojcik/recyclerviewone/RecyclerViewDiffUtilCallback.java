package com.example.mwojcik.recyclerviewone;

import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;

import java.util.List;


/***
 * DiffUtil sluzy do porownywania kolekcji, list. Dostepny od v24 w RecyclerView support v7.
 *DiffUtil requires O(N) space to find the minimal number of addition and removal operations
 * between the two lists. It’s expected performance is O(N + D²) where N is the total number
 * of added and removed items and D is the length of the edit script. You can walk through the
 * official page of Android for more performance figures.
 * Tak na marginesie, jak już mówiłem to używane jest jak zmienia nam się lista, a nie pojedyncze itemy.
 * Przykładem wykorzystania może być np. opcja z searchbarem na danym recyclerview.
 *
 *
 * Przykład czasowy performancu używania diffutol:
 * 100 items and 10 modifications: avg: 0.39 ms, median: 0.35 ms
 * 100 items and 100 modifications: 3.82 ms, median: 3.75 ms
 * 100 items and 100 modifications without moves: 2.09 ms, median: 2.06 ms
 * 1000 items and 50 modifications: avg: 4.67 ms, median: 4.59 ms
 * 1000 items and 50 modifications without moves: avg: 3.59 ms, median: 3.50 ms
 * 1000 items and 200 modifications: 27.07 ms, median: 26.92 ms
 * 1000 items and 200 modifications without moves: 13.54 ms, median: 13.36 ms
 * Max List size 2^26.
 */
public class RecyclerViewDiffUtilCallback extends DiffUtil.Callback {

    private List<Model> mOld;
    private List<Model> mNew;

    //poczebny nam konstruktor żeby ładnie poustawiać listy
    public RecyclerViewDiffUtilCallback(List<Model> mOld, List<Model> mNew){
        this.mOld = mOld;
        this.mNew = mNew;
    }

    @Override
    public int getOldListSize() {
        return mOld.size();
    }

    @Override
    public int getNewListSize() {
        return mNew.size();
    }

    /***
     * Wywoływana przez DiffUtil do zdecydowania czy dwa obiekty reprezentują ten sam Item. Jak nasze itemy
     * mają unikalne id, to powinniśmy sprawdzać w tej metodize równość ich id.
     */
    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        // Tutaj zalozyłem, że każdy item ma inne ID!! Generalnie tak być powinno. no chyba
        //że updejtujemy jakiś item
        boolean debugB = mOld.get(oldItemPosition).getId() == mNew.get(newItemPosition).getId();
        return mOld.get(oldItemPosition).getId() == mNew.get(newItemPosition).getId();
    }

    /***
     * Sprawdza czy dwa itemy mają te same dane. Wywoływane przez DiffUtil tylko jeżeli areItemsTheSame zwróci prawdę.
     */
    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        Model oldModel = mOld.get(oldItemPosition);
        Model newModel = mNew.get(newItemPosition);

        if(oldModel.getTitle().equals(newModel.getTitle()) && oldModel.getDescription().equals(newModel.getDescription())) {
            return true;
        }
        return false;
    }

    /***
     * Jeżeli areItemTheSamge zwróci prawdę i areContentsTheSame zwróci false, to DiffUtil wywoła te metode go uzyskania
     * payloadu o zmianach. Nie trzeba tego używać.
     * Obiekt zwracany przez te metode jest dispatchowany z DiffResult używająć notifyItemRangeChanged(position,count,payload), ktory
     * wywolywany jest w onBindViewHoldera adaptera.
     * W dokumentacji zasugerowane jest, że DiffUtil może potrzebować trochę czasu dla dużych zestawów danych i
     * rekomendowane jest żeby przenieść takie kalkulacje do backgrouind threada.
     * Przyklad wykorzystania + tez w background threadzie: https://proandroiddev.com/diffutil-is-a-must-797502bc1149
     * Przyklad sortowania:
     * https://medium.com/@nullthemall/sort-sortedlist-by-different-criteria-812003ba6f1
     *
     * Przykald z wideło żeby ładnie się to odbywało animacja itp: https://geoffreymetais.github.io/code/diffutil/
     *
     */
    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        return super.getChangePayload(oldItemPosition, newItemPosition);
    }
}
