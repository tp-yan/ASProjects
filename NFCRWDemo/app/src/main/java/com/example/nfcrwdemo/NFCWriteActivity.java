package com.example.nfctest;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.RemoteController;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.nfc.tech.NfcA;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.nio.charset.Charset;

import static android.content.Intent.CATEGORY_BROWSABLE;
import static android.content.Intent.CATEGORY_DEFAULT;

/**
 * 除文本写入外，其他只支持NDEF
 * <p>
 * 此类并未继承BaseNFC 请注意
 */

public class NFCWriteActivity extends AppCompatActivity {
    TextView tv1, tv2;
    EditText etSector, etBlock, etData;
    // private NfcAdapter nfcAdapter;


    private String[][] mTechLists;
    private int mCount = 0;
    String info = "";

    private int bIndex;
    private int bCount;

    private int BlockData;
    private String BlockInfo;
    private RadioButton mRead, mWriteData, mChange;
    private byte[] b3;
    byte[] code = MifareClassic.KEY_NFC_FORUM;//读写标签中每个块的密码
    private byte[] data3, b0;
    private String temp = "";

    private Context mContext;
    int block[] = {4, 5, 6, 8, 9, 10, 12, 13, 14, 16, 17, 18, 20, 21, 22, 24,
            25, 26, 28, 29, 30, 32, 33, 34, 36, 37, 38, 40, 41, 42, 44, 45, 46,
            48, 49, 50, 52, 53, 54, 56, 57, 58, 60, 61, 62};


    //----------------------------------------------------------------------
    private PendingIntent mPendingIntent;

    private IntentFilter[] mFilters;

    private RadioGroup radioGroup;

    private NfcAdapter mNfcAdapter;

    private String[] type_list = new String[]{"IsoDep", "NfcA", "NfcB", "NfcF", "NfcV", "Ndef", "NdefFormatable", "MifareUltralight", "MifareClassic"};

    private String NFC_type = "Ndef";

    private static final String TAG = "NFCWriteActivity.class";

    private Tag tag;

    private TextView tv_hint;

    private AlertDialog alertDialog;

    private EditText et_baoming;
    private EditText et_content;
    private EditText et_url;

    private String mNfctagName = ""; //打开命令中的包名
    private String action = "order"; //默认动作是写入命令

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_home);

        mContext = this;

        init();

        checkNFCFunction(); // NFC Check

//      PendingIntent 是在将来的某个时刻发生的。 此activity 实例化时 设置其启动方式，则清单中不用设置
        mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
                getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);


/**

 NFC数据过滤

 NFC有三种过滤器分别是ACTION_NDEF_DISCOVERED，ACTION_TECH_DISCOVERED，ACTION_TAG_DISCOVERED。

 1. ACTION_NDEF_DISCOVERED

 当扫描到的tag中包含有NDEF载荷且为已知类型，该intent将用来启动Activity。该intent的优先级最高，tag分发系统总是先于其他intent用该intent来启动Activity。

 2. ACTION_TECH_DISCOVERED

 如果manifest中没有注册处理ACTION_NDEF_DISCOVERED类型的intent，该intent将被用以启动Activity。如果tag中没有包含可以映射到MIME或者URI类型的数据，或者虽然没有包含NDEF数据，但是已知的tag技术，则该intent也会被直接启用。

 3. ACTION_TAG_DISCOVERED

 如果以上两个intent都没人疼，那么该intent就会启动。

 过滤器的作用是过滤掉杂质，剩下的就是我们需要的了。这三种过滤方式可同时配置，可以比方成从上到下三层，只要是符合某一层过滤器要求的，过滤完就停止往下一层。
 */

        // Setup an intent filter for all MIME based dispatches
        IntentFilter ndef1 = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        IntentFilter ndef2 = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
        IntentFilter ndef3 = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);

        try {
            //
            ndef1.addCategory(Intent.CATEGORY_DEFAULT);
            ndef2.addDataType("*/*");
        } catch (Exception e) {

        }

        mFilters = new IntentFilter[]{ndef1, ndef2, ndef3};

        // 根据标签类型设置
        mTechLists = new String[][]{new String[]{NfcA.class.getName()}};
    }

    //1:
    private void init() {
        tv1 = (TextView) findViewById(R.id.tv1);
        tv2 = (TextView) findViewById(R.id.tv2);
        tv_hint = (TextView) findViewById(R.id.tv_hint);
        et_baoming = (EditText) findViewById(R.id.et_baoming);
        et_content = (EditText) findViewById(R.id.et_content);
        et_url = (EditText) findViewById(R.id.et_url);

        radioGroup = findViewById(R.id.radio_group);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.radio_btn_0:
                        NFC_type = type_list[0];
                        break;
                    case R.id.radio_btn_1:
                        NFC_type = type_list[1];
                        break;
                    case R.id.radio_btn_2:
                        NFC_type = type_list[2];
                        break;
                    case R.id.radio_btn_3:
                        NFC_type = type_list[3];
                        break;
                    case R.id.radio_btn_4:
                        NFC_type = type_list[4];
                        break;
                    case R.id.radio_btn_5:
                        NFC_type = type_list[5];
                        break;
                    case R.id.radio_btn_6:
                        NFC_type = type_list[6];
                        break;
                    case R.id.radio_btn_7:
                        NFC_type = type_list[7];
                        break;
                    case R.id.radio_btn_8:
                        NFC_type = type_list[8];
                        break;
                }
            }
        });

    }

    //2:检测设备是否支持NFC
    private void checkNFCFunction() {
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (mNfcAdapter == null) {
            tv_hint.setText("NFC apdater is not available,不支持NFC");
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setIcon(R.mipmap.ic_launcher)
                    .setTitle("很遗憾")
                    .setMessage("没发现NFC设备，请确认您的设备支持NFC功能!")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            if (null != alertDialog) alertDialog.dismiss();
                        }
                    });
            alertDialog = builder.create();
            alertDialog.show();

            return;
        } else {
            if (!mNfcAdapter.isEnabled()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setIcon(R.mipmap.ic_launcher)
                        .setTitle("提示")
                        .setMessage("请确认NFC功能是否开启!")
                        .setPositiveButton("现在去开启......", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                alertDialog.dismiss();
                                Intent setnfc = new Intent(
                                        Settings.ACTION_NFC_SETTINGS);
                                startActivity(setnfc);
                            }
                        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        alertDialog.dismiss();
                    }
                });
                alertDialog = builder.create();
                alertDialog.show();
                return;
            }
        }
    }



//---------------------------------------------------------------------------------
//-------------------------------------------系统方法----------------------------------
//---------------------------------------------------------------------------------


    @Override
    protected void onResume() {
        super.onResume();
        if (mNfcAdapter != null) {
            mNfcAdapter.enableForegroundDispatch(this, mPendingIntent,
                    mFilters, mTechLists);
        }
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        tv1.setText("发现新的 Tag:  " + ++mCount + "\n");// mCount 计数

        String intentActionStr = intent.getAction();// 获取到本次启动的action

        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intentActionStr)// NDEF类型
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(intentActionStr)// 其他类型
                || NfcAdapter.ACTION_TAG_DISCOVERED.equals(intentActionStr)) {// 未知类型

            // 在intent中读取Tag id
            tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

            byte[] bytesId = tag.getId();// 获取id数组

            info += Util.ByteArrayToHexString(bytesId) + "\n";

            tv2.setText("标签UID:  " + "\n" + info);

        }

        switch (action) {
            case "order":
                if (TextUtils.isEmpty(mNfctagName)){
                    Toast.makeText(mContext, "请选择命令", Toast.LENGTH_SHORT).show();
                    return;
                }
                NdefMessage ndefMessage = new NdefMessage(new NdefRecord[]{
                        NdefRecord.createApplicationRecord(mNfctagName)});
                writeNFCTag(tag, ndefMessage);
                break;

            case "url":
                NdefMessage ndefMessage3 = new NdefMessage(new NdefRecord[]{NdefRecord
                        .createUri(Uri.parse(et_url.getText().toString().trim()))});
                writeNFCTag(tag, ndefMessage3);
                break;
            case "content": //向NFC卡写入不同格式的内容，此内容仅为文本内容，其他内容请参考其他资料
                chooseType(tag);
                break;

        }

    }

    private void chooseType(Tag tag) {
        String content = et_content.getText().toString();

        switch (NFC_type) {
            case "IsoDep":
                break;
            case "NfcA":

                break;
            case "NfcB":
                break;
            case "NfcF":
                break;
            case "NfcV":
                break;
            case "Ndef":
                NdefRecord[] records = {Util.createTextRecord(content)};

                writeNFCTag(tag, new NdefMessage(records));
                break;
            case "NdefFormatable":
                break;
            case "MifareUltralight":
                writeMifareUltralightTag(tag,content);
                break;
            case "MifareClassic":
                writeMifareClassicTag(tag, content);
                break;
        }
    }



//---------------------------------------------------------------------------------
//-----------------------------------------写入---------------------------------
//---------------------------------------------------------------------------------

    /**
     * Step6:往NDEF格式标签写入命令
     *
     * @param tag
     */
    public void writeNFCTag(Tag tag, NdefMessage ndefMessage) {
        if (tag == null) {
            Toast.makeText(this, "不能识别的标签类型", Toast.LENGTH_SHORT).show();
            return;
        }

        //转换成字节获得大小
        int size = ndefMessage.toByteArray().length;

        try {//判断NFC标签的数据类型（通过Ndef.get方法）
            Ndef ndef = Ndef.get(tag);
            //判断是否为NDEF标签
            if (ndef != null) {
                ndef.connect();
                //判断是否支持可写
                if (!ndef.isWritable()) {

                    Toast.makeText(this, "该标签不能写入数据", Toast.LENGTH_SHORT).show();

                    return;
                }
                //判断标签的容量是否够用
                if (ndef.getMaxSize() < size) {
                    Toast.makeText(this, "该标签容量不够", Toast.LENGTH_SHORT).show();

                    return;
                }
                //3.写入数据
                ndef.writeNdefMessage(ndefMessage);
                Toast.makeText(this, "写入数据成功", Toast.LENGTH_SHORT).show();
            } else {//当我们买回来的NFC标签是没有格式化的，或者没有分区的执行此步
                // Ndef格式类
                NdefFormatable format = NdefFormatable.get(tag);
                //判断是否获得了NdefFormatable对象，有一些标签是只读的或者不允许格式化的
                if (format != null) {
                    //连接
                    format.connect();
                    //格式化并将信息写入标签
                    format.format(ndefMessage);
                    Toast.makeText(this, "写入数据成功", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "写入数据失败", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {

        }
    }

    /**
     * step8: 写入MifareUltralight格式文本数据
     *
     * @param tag
     * @param str
     */
    private void writeMifareUltralightTag(Tag tag ,String str) {

        if (str.length() < 8){
            Toast.makeText(this, "请写入8个以上汉字", Toast.LENGTH_SHORT).show();
            return;
        }


        MifareUltralight ultralight = MifareUltralight.get(tag);
        try {
            ultralight.connect();
            //写入八个汉字，从第五页开始写，中文需要转换成GB2312格式
            ultralight.writePage(4, str.substring(0,2).getBytes(Charset.forName("GB2312")));
            ultralight.writePage(5, str.substring(2,4).getBytes(Charset.forName("GB2312")));
            ultralight.writePage(6, str.substring(4,6).getBytes(Charset.forName("GB2312")));
            ultralight.writePage(7, str.substring(6,8).getBytes(Charset.forName("GB2312")));

            Toast.makeText(this, "写入成功", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
        } finally {
            try {
                ultralight.close();
            } catch (Exception e) {
            }
        }
    }


    /**
     * step9: 写入MifareClassic格式文本数据
     *
     * @param tag
     * @param str
     */
    public void writeMifareClassicTag(Tag tag, String str) {
        MifareClassic mfc = MifareClassic.get(tag);
        try {
            if (mfc != null) {
                mfc.connect();
            } else {
                Toast.makeText(mContext, "写入失败", Toast.LENGTH_LONG).show();
                return;
            }
            Log.i("write", "----connect-------------");
            boolean CodeAuth = false;
            byte[] b1 = str.getBytes();
            if (b1.length <= 720) {
                //System.out.println("------b1.length:" + b1.length);
                int num = b1.length / 16;
                System.out.println("num= " + num);
                int next = b1.length / 48 + 1;
                System.out.println("扇区next的值为" + next);
                b0 = new byte[16];
                if (!(b1.length % 16 == 0)) {
                    for (int i = 1, j = 1; i <= num; i++) {
                        CodeAuth = mfc.authenticateSectorWithKeyA(j, code);
                        System.arraycopy(b1, 16 * (i - 1), b0, 0, 16);
                        mfc.writeBlock(block[i - 1], b0);
                        if (i % 3 == 0) {
                            j++;
                        }
                    }
                    //Log.d("下一个模块", "测试");
                    CodeAuth = mfc.authenticateSectorWithKeyA(next,// 非常重要------
                            code);
                    //Log.d("获取第5块的密码", "---成功-------");
                    byte[] b2 = {0};
                    b0 = new byte[16];
                    System.arraycopy(b1, 16 * num, b0, 0, b1.length % 16);
                    System.arraycopy(b2, 0, b0, b1.length % 16, b2.length);
                    mfc.writeBlock(block[num], b0);
                    mfc.close();
                    Toast.makeText(this, "写入成功", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    for (int i = 1, j = 1; i <= num; i++) {
                        if (i % 3 == 0) {
                            j++;
                            System.out.println("扇区j的值为：" + j);
                        }
                        CodeAuth = mfc.authenticateSectorWithKeyA(j,// 非常重要---------
                                code);
                        System.arraycopy(b1, 16 * (i - 1), b0, 0, 16);
                        mfc.writeBlock(block[i - 1], b0);
                        str += Util.ByteArrayToHexString(b0);
                        System.out.println("Block" + i + ": " + str);
                    }
                    mfc.close();
                    Toast.makeText(this, "写入成功", Toast.LENGTH_SHORT).show();
                    return;
                }
            } else {
                Toast.makeText(getBaseContext(), "字符过长，内存不足", Toast.LENGTH_SHORT).show();
                return;
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                mfc.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }


    public int StringToInt(String s) {
        if (!(TextUtils.isEmpty(s)) || s.length() > 0) {
            BlockData = Integer.parseInt(s);
        } else {
            Toast.makeText(NFCWriteActivity.this, "Block输入有误", Toast.LENGTH_LONG).show();
        }
        System.out.println(BlockData);
        return BlockData;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mNfcAdapter != null) {
            mNfcAdapter.disableForegroundDispatch(this);
        }
    }




    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.open_weixin:
                mNfctagName = "com.tencent.mm";
                action = "order";
                break;
            case R.id.open_qq:
                mNfctagName = "com.tencent.mobileqq";
                action = "order";
                break;
            case R.id.open_:
                String trim = et_baoming.getText().toString().trim();
                if (TextUtils.isEmpty(trim)) {
                    Toast.makeText(mContext, "请输入包名", Toast.LENGTH_SHORT).show();
                    return;
                }
                mNfctagName = trim;
                action = "order";
                break;
            case R.id.write_content:
                String trim2 = et_content.getText().toString().trim();
                if (TextUtils.isEmpty(trim2)) {
                    Toast.makeText(mContext, "内容不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                action = "content";
                break;
            case R.id.write_url:
                String trim3 = et_url.getText().toString().trim();
                if (TextUtils.isEmpty(trim3)) {
                    Toast.makeText(mContext, "网址不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                action = "url";
                break;
        }
    }


    //----------------------------------------------------------------------------------
//-------------------------------------------工具方法和类--------------------------------
//----------------------------------------------------------------------------------



    private Dialog SetDialogWidth(Dialog dialog) {
        // TODO 自动生成的方法存根
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screenWidth = dm.widthPixels;
        int screenHeight = dm.heightPixels;
        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        if (screenWidth > screenHeight) {
            params.width = (int) (((float) screenHeight) * 0.875);

        } else {
            params.width = (int) (((float) screenWidth) * 0.875);
        }
        dialog.getWindow().setAttributes(params);

        return dialog;
    }


}