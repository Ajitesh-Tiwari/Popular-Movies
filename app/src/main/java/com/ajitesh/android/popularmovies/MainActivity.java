package com.ajitesh.android.popularmovies;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.ajitesh.android.popularmovies.DetailViews.MovieDetailActivity;
import com.ajitesh.android.popularmovies.DetailViews.TabletDetailFragment;
import com.ajitesh.android.popularmovies.MovieGridLayouts.MovieFavFragment;
import com.ajitesh.android.popularmovies.MovieGridLayouts.MovieGridFragment;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements MovieGridFragment.OnItemSelectedListener,TabletDetailFragment.RefreshGrid {


    private String TAG="MainActivity";
    private TabLayout tabLayout;
    private ViewPager viewPager;
    Toolbar toolbar;
    FragmentManager fm;
    ViewPagerAdapter adapter;

    @Override
    public void refreshFavGrid() {
        adapter.notifyDataSetChanged();
    }

    public interface UpdateableFragment {
        public void update();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        fm = getSupportFragmentManager();
        if(getResources().getBoolean(R.bool.dual_pane)){
            tabletView();
        }
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

    }

    private void setupViewPager(ViewPager viewPager) {
        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        MovieGridFragment mostPopular=new MovieGridFragment();
        MovieGridFragment topRated=new MovieGridFragment();
        Bundle b1=new Bundle();
        b1.putString("sort_by","popularity.desc");
        Bundle b2=new Bundle();
        b2.putString("sort_by", "vote_average.desc&vote_count.gte=100");
        mostPopular.setArguments(b1);
        topRated.setArguments(b2);
        adapter.addFragment(mostPopular, "MOST POPULAR");
        adapter.addFragment(topRated, "TOP RATED");
        adapter.addFragment(new MovieFavFragment(),"MY FAVOURITES");
        viewPager.setAdapter(adapter);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        toolbar.inflateMenu(R.menu.menu_main);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent=new Intent(this,AboutUs.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMovieSelected(Movie movie) {
        boolean dual_pane = getResources().getBoolean(R.bool.dual_pane);
        if (dual_pane) {
            TabletDetailFragment tabletDetailFragment=new TabletDetailFragment();
            Bundle b1=new Bundle();
            b1.putParcelable("movie",movie);
            tabletDetailFragment.setArguments(b1);
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.detailFragment, tabletDetailFragment);
            ft.commit();
        } else {
            Intent intent = new Intent(this, MovieDetailActivity.class);
            intent.putExtra("Movie", movie);
            startActivity(intent);

        }
    }
    public void tabletView()
    {
        FragmentTransaction ft = fm.beginTransaction();
        ft.add(R.id.detailFragment, new FragmentNone());
        ft.commit();
    }


    class ViewPagerAdapter extends FragmentStatePagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }
        @Override
        public int getItemPosition(Object object) {
            if (object instanceof UpdateableFragment) {
                ((UpdateableFragment) object).update();
            }
            //don't return POSITION_NONE, avoid fragment recreation.
            return super.getItemPosition(object);
        }
        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(adapter!=null)
            adapter.notifyDataSetChanged();
    }
}
