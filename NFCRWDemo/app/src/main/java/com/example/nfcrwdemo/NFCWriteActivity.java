package com.example.nfcrwdemo;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
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
import java.util.Arrays;

/**
 * 除文本写入外，其他只支持NDEF
 * <p>
 * 此类并未继承BaseNFC 请注意
 */

public class NFCWriteActivity extends AppCompatActivity {

    private String[][] mTechLists;
    String info = "";

    private Context mContext;

    //----------------------------------------------------------------------
    private PendingIntent mPendingIntent;
    private IntentFilter[] mFilters;
    private NfcAdapter mNfcAdapter;
    private static final String TAG = "NFCWriteActivity.class";

    private Tag tag;
    private AlertDialog alertDialog;
    private EditText et_content;

    private TextView tvMD5, tvSha256, tvShuffleSHA256, tvRestoreSHA256;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_nfc);

        mContext = this;

        init();
        checkNFCFunction(); // NFC Check

        // PendingIntent 是在将来的某个时刻发生的。 此activity 实例化时 设置其启动方式，则清单中不用设置
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
            ndef1.addCategory(Intent.CATEGORY_DEFAULT);
            ndef2.addDataType("*/*");
        } catch (Exception e) {
            Log.e(TAG, "onCreate: ", e);
        }

        mFilters = new IntentFilter[]{ndef1, ndef2, ndef3};

        // 根据标签类型设置
        mTechLists = new String[][]{new String[]{NfcA.class.getName()}};
    }

    //1:
    private void init() {
        et_content = (EditText) findViewById(R.id.et_content);
        tvMD5 = findViewById(R.id.md5);
        tvSha256 = findViewById(R.id.sha256);
        tvShuffleSHA256 = findViewById(R.id.shuffle_sha256);
        tvRestoreSHA256 = findViewById(R.id.restore_sha256);
    }

    //2:检测设备是否支持NFC
    private void checkNFCFunction() {
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (mNfcAdapter == null) {
            Toast.makeText(mContext, "NFC apdater is not available,不支持NFC", Toast.LENGTH_SHORT).show();
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


//-------------------------------------------系统方法----------------------------------

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

        String intentActionStr = intent.getAction();// 获取到本次启动的action

        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intentActionStr)// NDEF类型
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(intentActionStr)// 其他类型
                || NfcAdapter.ACTION_TAG_DISCOVERED.equals(intentActionStr)) {// 未知类型

            // 在intent中读取Tag id
            tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

            byte[] bytesId = tag.getId();// 获取id数组

            info += Util.ByteArrayToHexString(bytesId) + "\n";
            Log.d(TAG, "NFC UID: " + info);
        }

        //向NFC卡写入不同格式的内容，此内容仅为文本内容，其他内容请参考其他资料
        chooseType(tag);
    }

    // 这里只写入NDEF格式的文本
    private void chooseType(Tag tag) {
        String content = et_content.getText().toString().trim();
        if (TextUtils.isEmpty(content)) {
            Toast.makeText(mContext, "请填写内容!", Toast.LENGTH_SHORT).show();
            return;
        }
        handle_key(content);
        NdefRecord[] records = {Util.createTextRecord(content)};
        writeNFCTag(tag, new NdefMessage(records));
    }

    // 密钥处理
    private void handle_key(String str) {
        byte[] md5 = Util.encode("MD5", str);
        byte[] sha256 = Util.encode("SHA-256", str);
        byte[] shuffleSha256 = Util.shuffleSHA256(sha256.clone(), null);
        byte[] restoreSha256 = Util.shuffleSHA256(shuffleSha256.clone(), null);
        String md5Str = Util.ByteArrayToHexString(md5);
        String sha256Str = Util.ByteArrayToHexString(sha256);
        String shuffleSha256Str = Util.ByteArrayToHexString(shuffleSha256);
        String restoreSha256Str = Util.ByteArrayToHexString(restoreSha256);

        tvMD5.setText(md5Str);
        tvSha256.setText(sha256Str);
        tvShuffleSHA256.setText(shuffleSha256Str);
        tvRestoreSHA256.setText(restoreSha256Str);

        Log.d(TAG, "handle_key: md5 " + md5Str); // 这里得到的是十六进制字符个数，一个字符对应一个byte
        Log.d(TAG, "handle_key: sha256 " + sha256Str);
        Log.d(TAG, "handle_key: shuffleSha256 " + shuffleSha256Str);
        Log.d(TAG, "handle_key: restoreSha256 " + restoreSha256Str);
        Log.d(TAG, "handle_key: sha256 == restoreSha256 ?" + Arrays.equals(sha256, restoreSha256));
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

    @Override
    public void onPause() {
        super.onPause();
        if (mNfcAdapter != null) {
            mNfcAdapter.disableForegroundDispatch(this);
        }
    }

}