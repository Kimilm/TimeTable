package com.example.kimilm.timetable;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import com.dekoservidoni.omfm.OneMoreFabMenu;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class TimeTableFragment extends Fragment implements View.OnClickListener{

//    ArrayList<TimeTable> timeTables;    //굳이 어레이리스트를 써야할까?
    FrameLayout frameLayout;
    GridLayout gridLayout;
    OneMoreFabMenu fab;
    ScrollView scrollView;

    ArrayList<Document> documents;

    BottomSheetDialog modalBottomSheet;

    Animation visib;
    Animation invisib;
    boolean checker; // fab.setVisibility(View.VISIBLE)가 최하단이 아닌 모든 스크롤 위치에서 작동하기 때문에 이를 막기 위한 변수.


    public TimeTableFragment(){
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if(container == null){
            return null;
        }
        View view = inflater.inflate(R.layout.fragment_time_table, container, false);

        frameLayout = (FrameLayout)view.findViewById(R.id.frame);

        gridLayout = (GridLayout)view.findViewById(R.id.gridLayout);

        scrollView = (ScrollView)view.findViewById(R.id.scrollView);
        fab = (OneMoreFabMenu)view.findViewById(R.id.faButton);
        fab.setOnClickListener(this);
        visib = AnimationUtils.loadAnimation(getActivity(), R.anim.visib);
        invisib = AnimationUtils.loadAnimation(getActivity(), R.anim.invisib);

        checker = false;

        //스크롤시 FloatingActionButton이 사라지는 코드
        scrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {    // ScrollView에서 스크롤이 화면 최하단에 도달함을 감지하는 코드.
            @Override
            public void onScrollChanged() {
                if (scrollView != null) {
                    if (scrollView.getChildAt(0).getBottom() <= (scrollView.getHeight() + scrollView.getScrollY())) {
                        // scroll view가 최하단에 도달함.
                        //fab.setVisibility(View.INVISIBLE);
                        //fab.startAnimation(invisib);
                        checker = true;
                    } else {
                        // scroll view가 최하단이 아님.
                        if(checker == true) {
                            //fab.setVisibility(View.VISIBLE);
                            //fab.startAnimation(visib);
                            checker = false;
                        }
                    }
                }
            }
        });

        setGridLayoutHeight();  //화면 사이즈에 맞게 변환하는 메소드.

        setHasOptionsMenu(true);
        documents = new ArrayList<>();

        new Thread() {
            @Override
            public void run() {
                mongo(documents, null, null);
            }
        }.start();

        View v = view.findViewById(R.id.mon);
        v.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                popInsertLessonFragment(v);
            }
        });

        //toImage
        v = view.findViewById(R.id.tue);
        v.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                toImage(v);
            }
        });

        setHasOptionsMenu(true);

        return view;
    }

    // OneMoreFabMenu의 메뉴 아이템의 아이디를 가져오는 코드 ↓

    @Override
    public void onResume() {
        super.onResume();
        getActivity().invalidateOptionsMenu();
    }

    MenuItem item1;
    MenuItem item2;
    MenuItem item3;



    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_time_table_fab_items, menu);

        item1 = menu.findItem(R.id.option1);
        item2 = menu.findItem(R.id.option2);
        item3 = menu.findItem(R.id.option3);

//        item1.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
//            @Override
//            public boolean onMenuItemClick(MenuItem item) {
//                popInsertLessonFragment();
//                return false;
//            }
//        });
//
//        item2.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
//            @Override
//            public boolean onMenuItemClick(MenuItem item) {
//                toImage();
//                return false;
//            }
//        });

        super.onCreateOptionsMenu(menu, inflater);
/*
        item1.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                Toast.makeText(getActivity(), "13243546576454", Toast.LENGTH_LONG).show();
                return true;
            }
        });

        item2.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                Toast.makeText(getActivity(), "13243546576454", Toast.LENGTH_LONG).show();
                return true;
            }
        });
        item3.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                Toast.makeText(getActivity(), "SDYRTHEGRWSV", Toast.LENGTH_LONG).show();
                return true;
            }
        });
*/
    }
/*
    @Override
    public boolean onOptionsItemSelected(MenuI
                        Toast.makeText(getActivity(), "비교시간표 변경 선택됨!", Toast.LENGTH_LONG).show();
                        return false;tem item) {
                        return super.onOptionsItemSelected(item);
                    }
*/


//    @Override
//    public boolean onOptionsItemSelected(MenuItem item)
//    {
//        int curId = item.getItemId();
//        switch(curId)
//        {
//            case R.id.option1:
//                popInsertLessonFragment();
//                return false;
//            case R.id.option2:
//                toImage();
//                return false;
//            case R.id.option3:
//                Toast.makeText(getActivity(), "이미지로 저장 선택됨!", Toast.LENGTH_LONG).show();
//                return false;
//            default:
//                break;
//        }
//    return false;
//    }

    @Override
    public void onOptionsMenuClosed(Menu menu) {
        super.onOptionsMenuClosed(menu);
    }

    @Override
    public void onClick(View v) {
        /* Snackbar : 간단한 문자열 메시지를 사용자에게 잠깐 보여줄 목적으로 사용
           - Toast 메시지와 비슷하지만, 사용자의 이벤트 처리가 가능하기 때문에 많이 사용
           - Snackbar.make(스넥바가 뜨게될 View, 사용자에게 보일 문자열 메시지, 스넥바가 화면에 뜨는 시간)
           - setAction() 메서드를 사용하면, Snackbar에서 사용자 이벤트를 처리할 수 있음
           - setAction(Action문자열, 이벤트 핸들러)
           - 사용자가 Action문자열을 클릭하면, 두 번째 매개변수인 OnClickListener()를 구현한 이벤트 핸들러가 실행
        */
        Snackbar.make(v,"Snackbar with Action", Snackbar.LENGTH_LONG).setActionTextColor(Color.YELLOW).setAction("현재 시간?", new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                long now = System.currentTimeMillis();
                Date date = new Date(now);
                SimpleDateFormat dateformat = new SimpleDateFormat("HH:mm:ss MM/dd/yyyy", Locale.US);
                String getTime = dateformat.format(date);
                Toast.makeText(getActivity(), getTime, Toast.LENGTH_LONG).show();
            }
        }).show();
    }

    //화면 사이즈에 맞게 변환
    public void setGridLayoutHeight()
    {
        Display display = getActivity().getWindowManager().getDefaultDisplay(); // 프래그먼트 상에서 디스플레이 사이즈를 가져오기 위해 getActivity()를 추가함.
        Point size = new Point();
        display.getSize(size);

        ViewGroup.LayoutParams params = gridLayout.getLayoutParams();
        params.height = size.y;
        gridLayout.setLayoutParams(params);
    }

    public TimeTable getTable (View v)
    {
        TimeTable timeTable = new TimeTable();

        //로그인 혹은 로컬 디비에 저장되어있는 시간표를 불러온다.

        return timeTable;
    }


    //강의 띄우기
    public void showTable (Lesson lesson)
    {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        View [] view = new View[lesson.times.size()];

        int r = (int)(Math.random() * 255);
        int g = (int)(Math.random() * 255);
        int b = (int)(Math.random() * 255);

        for(int i = 0; i < lesson.times.size(); ++i)
        {
            LinearLayout wrapLayout = new LinearLayout(getContext());

            wrapLayout.setLayoutParams(layoutParams);

            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            view[i] = inflater.inflate(R.layout.lesson_view, wrapLayout,true);

            LinearLayout.LayoutParams viewParams = new LinearLayout.LayoutParams(setBtnWidth(lesson.times.get(i).substring(0, 1)),
                    setBtnHeight(lesson.times.get(i).substring(1)));

            viewParams.setMargins(setBtnLeftMargin(lesson.times.get(i).substring(0, 1)),
                    setBtnTopMargin(lesson.times.get(i).substring(1)), 0, 0);

            view[i].setLayoutParams(viewParams);

            view[i].setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v) {
                    BottomSheet(R.layout.modal_bottom_sheet_info_lesson, v);
                }
            });

            //랜덤 컬러
            view[i].setBackgroundColor(Color.rgb(r, g, b));

            ((TextView)(view[i].findViewById(R.id.floatTitle))).setText(lesson.title);
            ((TextView)(view[i].findViewById(R.id.floatClassRoom))).setText(lesson.classroom.toString().replace("[", "").replace("]", ""));
            ((TextView)(view[i].findViewById(R.id.floatProf))).setText(lesson.prof);

            ((TextView)(view[i].findViewById(R.id.floatClassify))).setText(lesson.classify);
            ((TextView)(view[i].findViewById(R.id.floatCredit))).setText(lesson.credit);
            ((TextView)(view[i].findViewById(R.id.floatCode))).setText(lesson.code);
            ((TextView)(view[i].findViewById(R.id.floatTimes))).setText(lesson.times.toString().replace("[", "").replace("]", ""));
            ((TextView)(view[i].findViewById(R.id.floatCount))).setText(String.valueOf(lesson.times.size()));

            //동적 뷰 제거 위해
            wrapLayout.setId(Integer.parseInt(lesson.code + i));

            frameLayout.addView(wrapLayout);
        }

        ((LinearLayout)view[0].findViewById(R.id.floatLessonTexts)).setVisibility(View.VISIBLE);
    }

//    time.substring(0, 1)
    public int setBtnLeftMargin(String time)
    {
        switch(time)
        {
            case "월":
                return getActivity().findViewById(R.id.mon).getLeft();
            case "화":
                return getActivity().findViewById(R.id.tue).getLeft();
            case "수":
                return getActivity().findViewById(R.id.wed).getLeft();
            case "목":
                return getActivity().findViewById(R.id.thr).getLeft();
            case "금":
                return getActivity().findViewById(R.id.fri).getLeft();
        }
        return 0;
    }

//    time.substring(0, 1)
    public int setBtnWidth(String time)
    {
        switch(time)
        {
            case "월":
                return getActivity().findViewById(R.id.mon).getWidth();
            case "화":
                return getActivity().findViewById(R.id.tue).getWidth();
            case "수":
                return getActivity().findViewById(R.id.wed).getWidth();
            case "목":
                return getActivity().findViewById(R.id.thr).getWidth();
            case "금":
                return getActivity().findViewById(R.id.fri).getWidth();
        }
        return 0;
    }

//    time.substring(1)
    public int setBtnTopMargin(String time)
    {
        //A, B, C, D, E 교시
        if (time.charAt(0) >= 65)
        {
            switch(time)
            {
                case "A":
                    return getActivity().findViewById(R.id.time1).getTop()
                            + Math.round(getActivity().findViewById(R.id.time1).getHeight() * 0.5f);
                case "B":
                    return getActivity().findViewById(R.id.time3).getTop();

                case "C":
                    return getActivity().findViewById(R.id.time4).getTop()
                            + Math.round(getActivity().findViewById(R.id.time4).getHeight() * 0.5f);
                case "D":
                    return getActivity().findViewById(R.id.time6).getTop();

                case "E":
                    return getActivity().findViewById(R.id.time7).getTop()
                            + Math.round(getActivity().findViewById(R.id.time7).getHeight() * 0.5f);
            }
        }
        //1 ~ 14 교시
        else
        {
            if(Integer.parseInt(time) < 9)
            {
                int resId = getResources().getIdentifier("time" + Integer.parseInt(time), "id", getActivity().getPackageName());
                return getActivity().findViewById(resId).getTop();
            }
            else
            {
                int resId = getResources().getIdentifier("time" + Integer.parseInt(time), "id", getActivity().getPackageName());
                return getActivity().findViewById(resId).getTop()
                        + (int)Math.floor(getActivity().findViewById(resId).getHeight() / 60f * (30 - ((Integer.parseInt(time) - 9) * 5)));
            }
        }
        return 0;
    }

    //    time.substring(1)
    public int setBtnHeight(String time)
    {
        //A, B, C, D, E 교시
        if (time.charAt(0) >= 65)
        {
            switch(time)
            {
                case "A":
                    return Math.round(getActivity().findViewById(R.id.time1).getHeight() * 1.5f);

                case "B":
                    return Math.round(getActivity().findViewById(R.id.time3).getHeight() * 1.5f);

                case "C":
                    return Math.round(getActivity().findViewById(R.id.time4).getHeight() * 1.5f);

                case "D":
                    return Math.round(getActivity().findViewById(R.id.time6).getHeight() * 1.5f);

                case "E":
                    return Math.round(getActivity().findViewById(R.id.time7).getHeight() * 1.5f);
            }
        }
        //1 ~ 14 교시
        else
        {
            if(Integer.parseInt(time) < 9)
            {
                int resId = getResources().getIdentifier("time" + Integer.parseInt(time), "id", getActivity().getPackageName());
                return getActivity().findViewById(resId).getHeight();
            }
            else
            {
                int resId = getResources().getIdentifier("time" + Integer.parseInt(time), "id", getActivity().getPackageName());
                return (int)Math.ceil(getActivity().findViewById(resId).getHeight() * 11 / 12.0);
            }
        }
        return 0;
    }

    public void popInsertLessonFragment (View v)
    {
        //프래그먼트 생성하고 강의 정보 넘김
        InsertLessonFragment insertLessonFragment = new InsertLessonFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("Doc", documents);
        insertLessonFragment.setArguments(bundle);

        //화면에 띄움
        FragmentManager fManager = getFragmentManager();

        FragmentTransaction fTransaction = fManager.beginTransaction();
        fTransaction.addToBackStack(null);
        fTransaction.replace(R.id.coordinator, insertLessonFragment).commit();
    }

    public void BottomSheet(int layoutId, View parent)
    {
        final View view = getLayoutInflater().inflate(layoutId, null);

        ((ImageView)view.findViewById(R.id.lessonExit)).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                modalBottomSheet.dismiss();
            }
        });

        // Delete Function
        ((ImageView)view.findViewById(R.id.lessonDelete)).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                TimeTable.delLesson(((TextView)view.findViewById(R.id.lessonCode)).getText().toString());

                int count = Integer.parseInt(((TextView)view.findViewById(R.id.lessonCount)).getText().toString());

                for(int i = 0; i < count; ++i)
                {
                    frameLayout.removeView(frameLayout.findViewById(Integer.parseInt(
                            ((TextView)view.findViewById(R.id.lessonCode)).getText().toString() + i)));
                }

                TimeTable.saveTable();

                modalBottomSheet.dismiss();
            }
        });

        ((TextView)view.findViewById(R.id.lessonTitle)).setText(((TextView)parent.findViewById(R.id.floatTitle)).getText());
        ((TextView)view.findViewById(R.id.lessonProf)).setText(((TextView)parent.findViewById(R.id.floatProf)).getText());
        ((TextView)view.findViewById(R.id.lessonTime)).setText(((TextView)parent.findViewById(R.id.floatTimes)).getText());
        ((TextView)view.findViewById(R.id.lessonCredit)).setText(((TextView)parent.findViewById(R.id.floatCredit)).getText());
        ((TextView)view.findViewById(R.id.lessonClassify)).setText(((TextView)parent.findViewById(R.id.floatClassify)).getText());
        ((TextView)view.findViewById(R.id.lessonClassroom)).setText(((TextView)parent.findViewById(R.id.floatClassRoom)).getText());
        ((TextView)view.findViewById(R.id.lessonCode)).setText(((TextView)parent.findViewById(R.id.floatCode)).getText());
        ((TextView)view.findViewById(R.id.lessonCount)).setText(((TextView)parent.findViewById(R.id.floatCount)).getText());

        modalBottomSheet = new BottomSheetDialog(getActivity());

        modalBottomSheet.setContentView(view);

        modalBottomSheet.show();
    }

    //권한 체크변경
    //시간표를 이미지로 저장하는 코드 ↓
    public void toImage (View v)
    {
        if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != getActivity().getPackageManager().PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != getActivity().getPackageManager().PERMISSION_GRANTED)
        {
            Toast.makeText(getActivity(), "저장소 권한이 없습니다.", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(getActivity(), new String [] {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 200);
        }

        /*화면 크기를 초과한 이미지는 BuildDrawingCache() 사용시 이미지를 읽어오지 못하는 문제가 있어서 Canvas 클래스 사용함*/
        //레이아웃 크기와 동일한 비트맵 생성

        Bitmap bitmap = Bitmap.createBitmap(frameLayout.getWidth(), frameLayout.getHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);

        frameLayout.draw(canvas);

        String folderPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "MyFolder";

        File folder = new File(folderPath);

        if (!folder.exists())
        {
            folder.mkdirs();
        }

        folderPath = folderPath + File.separator + System.currentTimeMillis() + ".png";

        folder = new File(folderPath);

        try
        {
            FileOutputStream output = new FileOutputStream(folder);

            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output);

            output.close();

            getActivity().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(folder)));

            Toast.makeText(getActivity(), "이미지 저장 완료", Toast.LENGTH_SHORT).show();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Toast.makeText(getActivity(), "이미지 저장 실패", Toast.LENGTH_SHORT).show();
        }
    }   // 이미지를 저장하는 코드의 끝 ↑

    // 이하는 몽고디비
    public static ArrayList<String> toSubString (String str)
    {
        ArrayList strArray = new ArrayList<>();

        str = str.replace("[", "").replace("]", "");

        String [] subStr = str.split(", ");

        for (String token : subStr)
        {
            strArray.add(token);
        }

        return strArray;
    }

    public static Lesson insertLesson(Document document)
    {
        ArrayList<String> [] strArray = new ArrayList[2];

        String code = document.get("_id").toString();
        String title = document.get("title").toString();
        String classify = document.get("classify").toString();
        String credit = document.get("credit").toString();
        strArray[0] = toSubString(document.get("times").toString());
        String prof = document.get("prof").toString();
        strArray[1] = toSubString(document.get("classroom").toString());

        return new Lesson(code, title, classify, credit, strArray[0], prof, strArray[1], 0);
    }

    public static void mongo (ArrayList<Document> document, String key, String value)
    {
        document.clear();

        String IP = "45.119.146.33";
        int Port = 27017;
        String dbName = "TimeTable";

        //Connect to MongoDB
        MongoClient mongoClient = new MongoClient(IP, Port);
        MongoDatabase db = mongoClient.getDatabase(dbName);

        //IT_ComputerEngineering code
        MongoCollection<Document> collection = db.getCollection("CJ0200");

        Document query = new Document();

        //search
        if(key != null && value != null)
        {
            query = new Document(key, new Document("$regex", value));
        }

        MongoCursor<Document> cursor = collection.find(query).iterator();


        while (cursor.hasNext())
        {
            document.add(cursor.next());
        }

        mongoClient.close();
    }
}
