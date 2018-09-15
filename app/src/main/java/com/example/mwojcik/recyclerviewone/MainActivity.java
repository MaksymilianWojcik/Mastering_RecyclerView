package com.example.mwojcik.recyclerviewone;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = (RecyclerView) findViewById(R.id.main_recycler_view);

        final List<Model> mockData = new ArrayList<>();
        mockData.add(new Model(1, "Zorro", "Opowiesc o zorro"));
        mockData.add(new Model(2, "Batman", "Opowiesc o batmanie"));
        mockData.add(new Model(4, "Spiderman", "Opowiesc o spidermanie"));
        mockData.add(new Model(5, "Wolverine", "Opowiesc o wolverinie"));
        mockData.add(new Model(6, "Silversurfer", "Opowiesc o silversurferze"));
        mockData.add(new Model(7, "Flash", "Opowiesc o flashu"));
        mockData.add(new Model(8, "Anakin Skywalker", "Opowiesc o darth vaderze"));
        mockData.add(new Model(9, "Superman", "Opowiesc o klarku kencie"));
        mockData.add(new Model(10, "Wonderwoman", "Opowiesc o wonderwoman"));
        mockData.add(new Model(11, "Magneto", "Opowiesc o magneto"));
        mockData.add(new Model(12, "Hulk", "Opowiesc o hulku"));

        RecyclerViewAdapter adapter = new RecyclerViewAdapter(mockData);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);

        adapter.setOnItemClickListener(new RecyclerViewAdapter.RecyclerViewOnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {
                Model model = mockData.get(position);
                Log.d("MainActivity", "Received itemClick from listener for item: " + model.getTitle());
            }
        });

        //Customowy itemDecoraton: https://gist.github.com/nesquena/db922669798eba3e3661
//        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        RecyclerViewCustomDivider itemDecoration = new RecyclerViewCustomDivider(16);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(itemDecoration);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        //Click listenery: https://gist.github.com/nesquena/231e356f372f214c4fe6



    }
}
