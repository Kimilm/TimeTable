package com.example.kimilm.timetable;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.bson.Document;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity implements TabLayout.OnTabSelectedListener {

    RelativeLayout container;
    ViewPager viewPager;
    TabLayout tabLayout;
    ActionBarDrawerToggle toggle;
    DrawerLayout drawer;
    LinearLayout account_window;
    public static Friend thisFr;
    public static boolean isCurAcc;
    public static String curAccId;

    MyPagerAdapter fragmentAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        thisFr = new Friend();
        container = (RelativeLayout) findViewById(R.id.container);
        fragmentAdapter = new MyPagerAdapter(getSupportFragmentManager());

        container = (RelativeLayout)findViewById(R.id.container);
        viewPager = (ViewPager)findViewById(R.id.viewPager);
        viewPager.setAdapter(fragmentAdapter);  // viewPager에 Adapter 설정

        drawer = (DrawerLayout)findViewById(R.id.drawer);

        tabLayout = (TabLayout)findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);    // tabLayout을 ViewPager와 연동.
        tabLayout.addOnTabSelectedListener(this);   // tabLayout의 이벤트 핸들러 등록.

        // MainActivity에 NavigationDrawer 설정하는 코드 ↓
        toggle=new ActionBarDrawerToggle(this, drawer, R.string.drawer_open, R.string.drawer_close); // Toggle 생성.

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);  // ActionBar에서 기본 홈 버튼을 사용 가능.

        toggle.syncState(); // ActionBarDrawerToggle의 상태를 sync

        // NavigationView에 이벤트 설정.
        NavigationView navigationView=(NavigationView)findViewById(R.id.main_drawer_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id=item.getItemId();

                if(id==R.id.nav_account)
                {   // 계정설정 클릭시 AccountActivity로 날아감.
                    Intent myIntent = new Intent(getApplicationContext(), AccountActivity.class);
                    myIntent.putExtra("isCurAcc", isCurAcc); // 디바이스에 계정 정보가 있으면 true, 없으면 false를 전달함.

                    if(isCurAcc == true)
                    {   // 디바이스 계정정보가 있으면 그 id를 전달.
                        myIntent.putExtra("curAccId", curAccId);
                    }
                    startActivityForResult(myIntent, 1000);
                    drawer.closeDrawer(Gravity.LEFT);
                }
                else if(id==R.id.nav_contact_mail)
                {
                    Toast.makeText(getApplicationContext(), "B", Toast.LENGTH_LONG).show();
                    drawer.closeDrawer(Gravity.LEFT);
                }
                else if(id==R.id.nav_contact_phone)
                {
                    Toast.makeText(getApplicationContext(), "C", Toast.LENGTH_LONG).show();
                    drawer.closeDrawer(Gravity.LEFT);
                }
                return false;
            }
        });

        // 디바이스 내에 계정 정보가 있으면 불러오는 코드 ↓
        File files = new File(getFilesDir(), "AccInDevice.json");

        if(files.exists()==true)
        {   // 만약 이미 존재하는 파일이 있으면 파일 불러오기.
            isCurAcc = true;
            FileReader fr = null;
            BufferedReader bufrd = null;
            char ch;
            try
            {
                String jsonStr = new String();

                fr = new FileReader(files);
                bufrd = new BufferedReader(fr);

                while((ch = (char)bufrd.read()) != -1)
                {
                    jsonStr += String.valueOf(ch);

                    if(ch == '}')
                    {
                        break;
                    }
                }

//                JSONObject jsonObj = new JSONObject(jsonStr);

                //몽고디비 Document 객체에서 어레이를 바로 뺄 수 있는 방법을 찾음
                Document jsonObj = Document.parse(jsonStr);

                curAccId = jsonObj.getString("_id");

                thisFr.setId(jsonObj.get("_id").toString());
                thisFr.setPw(jsonObj.get("pwd").toString());
                thisFr.setName(jsonObj.get("name").toString());

                //그 결과
                thisFr.setFrList((ArrayList<String>)(jsonObj.get("f_id", ArrayList.class)));
//                thisFr.setFrList(TimeTableFragment.toSubString(jsonObj.getJSONArray("f_id").toString()));

                bufrd.close();
                fr.close();
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
        else
        {  // 파일이 없다.
            isCurAcc = false;
            Toast.makeText(this, "계정 파일이 없음", Toast.LENGTH_LONG).show();
        }
    } // end of onCreate()

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // onOptionsItemSelected() Required. 이벤트가 toggle에서 발생한거라면 메뉴 이벤트 로직에서 벗어나게 처리.
        if(toggle.onOptionsItemSelected(item)){
            return false;
        }
        return super.onOptionsItemSelected(item);
    }

    /* OnTabSelectedListener의 콜백 메소드.
         - TabLayout의 탭 버튼을 사용자가 터치했을 때 이벤트를 처리하기 위한 콜백 메소드로,
           탭 버튼과 ViewPager 화면 조정을 setCurrentItem() 메소드로 처리.*/
    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        viewPager.setCurrentItem(tab.getPosition());
    }
    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }
    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    /* TabLayout과 연동하기 위한 ViewPager의 Adapter 클래스 선언 */
    class MyPagerAdapter extends FragmentStatePagerAdapter {
        List<Fragment> fragments=new ArrayList<>();//fragments ArrayList

        //탭 버튼 문자열 배열 -> 사용 안
        String title[]=new String[]{"", ""};

        //Adapter 생성자
        public MyPagerAdapter(FragmentManager fm){
            super(fm);
            //프래그먼트를 생성하여 ArrayList에 add
            fragments.add(new TimeTableFragment());
            fragments.add(new FriendsFragment());

            TimeTable.fragment = (TimeTableFragment) fragments.get(0);
        }

        /* 실제 ViewPager에서 보여질 프래그먼트를 반환
           - 일반 Adapter(리스트뷰 등)의 getView()와 같은 역할
           - @param position : ViewPager에서 보여져야할 페이지 값(0부터)
           - @return : 보여질 fragment
        */
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        //ViewPager에 보여질 총 페이지 수
        @Override
        public int getCount() {
            return fragments.size();
        }

        //getPageTitle() 메서드에서 반환한 문자열이 TabLayout의 버튼 문자열로 사용됨
        @Override
        public CharSequence getPageTitle(int position) {
            return title[position];
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 1000 && resultCode == RESULT_OK)
        {
            //변경 이후 세팅
            isCurAcc = data.getBooleanExtra("isCurAcc", true);

            if(!isCurAcc)
            {
                Toast.makeText(this, "계정이 없습니다.", Toast.LENGTH_SHORT);
                return;
            }

            //1 : createBtn  2 : loginBtn
            if(data.getIntExtra("btnType", 0) < 3)
            {
                Friend getFriend = (Friend) data.getSerializableExtra("friendInfo");
                thisFr.setId(getFriend.getId());
                thisFr.setPw(getFriend.getPw());
                thisFr.setName(getFriend.getName());
                thisFr.setFrList(getFriend.getFrList());
                curAccId = getFriend.getName();
            }
            //3 : logoutBtn  4 : deleteBtn
            else if(data.getIntExtra("btnType", 0) > 2)
            {
                thisFr = new Friend();
                TimeTable.resetData();
            }

            Toast.makeText(this, "AccountActivity가 정상적으로 종료됨." + isCurAcc, Toast.LENGTH_LONG).show();
        }
    }
}


