package com.example.manage.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.manage.R;
import com.example.manage.pojo.Entity;
import com.example.manage.Adapter.SheetAdapter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StartSignInMenuActivity extends AppCompatActivity {

    private RecyclerView rv_sheet_start_sign;
    private List<Entity> list = new ArrayList<Entity>();;
    private SheetAdapter sheetAdapter;

    private Connection conn = null;
    private ResultSet rs=null;
    private PreparedStatement st = null;

    private ImageButton bt_return_start_menu;
    private Button bt_sign_in_menu;
    private Spinner spinner_status;
    private Spinner spinner_apartment;

    //接受子线程的数据，并传输到sheetAdapter
    private Handler mHandler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                list = (List<Entity>) msg.obj;

                sheetAdapter = new SheetAdapter(list);
                rv_sheet_start_sign.setAdapter(sheetAdapter);
            }

        }
    };
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_start_sign_in_menu);

        initView();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    //连接数据库
                    Class.forName("com.mysql.jdbc.Driver");
                    String url="jdbc:mysql://rm-bp1v8goj767jxm00wdo.mysql.rds.aliyuncs.com/record?useUnicode=true&characterEncoding=UTF-8&useSSL=false";
                    String username = "root";
                    String password = "asd123456ROOT";
                    conn = DriverManager.getConnection(url, username, password);
                    //表名
                    SimpleDateFormat sdf= new SimpleDateFormat("yyyyMMdd");
                    String date = sdf.format(new Date());
                    String tableName = "record_"+date;

                    String sql = "select * from "+tableName;
                    st = conn.prepareStatement(sql);

                    //rs结果集以列表形式加到list_tmp
                    List<Entity> list_tmp = new ArrayList<>();
                    rs = st.executeQuery();
                    Entity entity = new Entity();
                    while (rs.next()) {
                        String name = rs.getString("name");
                        String phone = rs.getString("phone");
                        String startStatus = rs.getString("startstatus");
                        String startTime = rs.getString("starttime");
                        String startLocal = rs.getString("startlocal");
                        String apartment = rs.getString("apartment");
                        list_tmp.add(new Entity(name,phone,startStatus,startTime,startLocal,apartment));

                        //通过handler将子线程的数据传递到主线程
                        Message message = new Message();
                        message.what = 1;
                        message.obj = list_tmp;
                        mHandler.sendMessage(message);
                    }

                }catch (Exception e){
                    e.printStackTrace();
                }finally {
//                    try {
//                        conn.close();
//                        st.close();
//                        rs.close();
//                    } catch (SQLException throwables) {
//                        throwables.printStackTrace();
//                    }
                }
            }
        }).start();

        //返回按钮的点击监听
        bt_return_start_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(StartSignInMenuActivity.this, SelectActivity.class));
            }
        });

        bt_sign_in_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            //连接数据库
                            Class.forName("com.mysql.jdbc.Driver");
                            String url="jdbc:mysql://rm-bp1v8goj767jxm00wdo.mysql.rds.aliyuncs.com/record?useUnicode=true&characterEncoding=UTF-8&useSSL=false";
                            String username = "root";
                            String password = "asd123456ROOT";
                            conn = DriverManager.getConnection(url, username, password);
                            //表名
                            SimpleDateFormat sdf= new SimpleDateFormat("yyyyMMdd");
                            String date = sdf.format(new Date());
                            String tableName = "record_"+date;

                            String sql;
                            if (spinner_status.getSelectedItem().toString().equals("全部")) {
                                if (spinner_apartment.getSelectedItem().toString().equals("全部")) {
                                    sql = "select * from "+tableName;
                                    st = conn.prepareStatement(sql);
                                } else {
                                    sql = "select * from "+tableName+" where apartment = ?";
                                    st = conn.prepareStatement(sql);
                                    st.setString(1,apartmentSwitch(spinner_apartment.getSelectedItem().toString()));
                                }
                            } else {
                                if (spinner_apartment.getSelectedItem().toString().equals("全部")) {
                                    sql = "select * from "+tableName+" where startstatus = ?";
                                    st = conn.prepareStatement(sql);
                                    st.setString(1,startStatusSwitch(spinner_status.getSelectedItem().toString()));
                                } else {
                                    sql = "select * from "+tableName+" where startstatus = ? and apartment = ?";
                                    st = conn.prepareStatement(sql);
                                    st.setString(1,startStatusSwitch(spinner_status.getSelectedItem().toString()));
                                    st.setString(2,apartmentSwitch(spinner_apartment.getSelectedItem().toString()));
                                }
                            }

                            //rs结果集以列表形式加到list_tmp
                            List<Entity> list_tmp = new ArrayList<>();
                            rs = st.executeQuery();
                            Entity entity = new Entity();

                            while (rs.next()) {
                                String name = rs.getString("name");
                                String phone = rs.getString("phone");
                                String startStatus = rs.getString("startstatus");
                                String startTime = rs.getString("starttime");
                                String startLocal = rs.getString("startlocal");
                                String apartment = rs.getString("apartment");
                                list_tmp.add(new Entity(name,phone,startStatus,startTime,startLocal,apartment));

                                //通过handler将子线程的数据传递到主线程
                                Message message = new Message();
                                message.what = 1;
                                message.obj = list_tmp;
                                mHandler.sendMessage(message);
                            }
                            if (rs.first()==false) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(StartSignInMenuActivity.this,"未查询到相关数据",Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        }catch (SQLException throwables) {
                            throwables.printStackTrace();
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });
    }

    public void initView() {
        bt_return_start_menu = findViewById(R.id.bt_return_start_menu);
        bt_sign_in_menu = findViewById(R.id.bt_sign_in_menu);
        spinner_status = findViewById(R.id.spinner_status);
        spinner_apartment = findViewById(R.id.spinner_apartment);


        rv_sheet_start_sign = (RecyclerView) findViewById(R.id.rv_sheet_start_sign);
        //设置线性布局 Creates a vertical LinearLayoutManager
        rv_sheet_start_sign.setLayoutManager(new LinearLayoutManager(this));
        //设置recyclerView每个item间的分割线
        rv_sheet_start_sign.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL));
    }

    public String apartmentSwitch(String spinner) {

        String result = null;
        switch (spinner) {
            case "卫生所":
                result ="卫生所";
                break;
            case "公服中心":
                result ="公服中心";
                break;
            case "物管中心":
                result ="物管中心";
                break;
            case "工程维修部":
                result ="工程维修部";
                break;
            case "宿管中心":
                result ="宿管中心";
                break;
            case "饮服中心":
                result ="饮服中心";
                break;
            case "采供部":
                result ="采供部";
                break;
            case "财务部":
                result ="财务部";
                break;
            case "综合办":
                result ="综合办";
                break;
            case "管理层":
                result ="管理层";
                break;
        }
        return result;
    }

    public String startStatusSwitch(String spinner) {
        String result = null;

        switch (spinner) {
            case "未打卡":
                result = "未打卡";
                break;
            case "正常":
                result = "正常";
                break;
            case "迟到":
                result = "迟到";
                break;
        }

        return result;
    }
}

