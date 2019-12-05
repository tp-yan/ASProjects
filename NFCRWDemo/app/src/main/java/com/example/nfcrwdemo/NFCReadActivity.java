package com.example.nfcrwdemo;

import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.nfc.tech.NfcF;
import android.nfc.tech.NfcV;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import static com.example.nfcrwdemo.Util.ByteArrayToHexString;


public class NFCReadActivity extends BaseNFCActivity {

    private static String TAG = "NFCReadActivity.class";

    private TextView tv_uid; // NFC标签的id
    private TextView tv_content;
    private TextView tv_type; // NFC支持数据类型

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_nfc);

        initView();
    }

    private void initView() {
        tv_uid = findViewById(R.id.uid);
        tv_content = findViewById(R.id.content);
        tv_type = findViewById(R.id.type);
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Tag mTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

        String[] techList = mTag.getTechList();
        // 公共方法：
        /**
         * NFC卡支持的标签类型：
         */
        String name = "";
        for (int i = 0; i < techList.length; i++) {
            name = name + techList[i] + "\n";
        }
        tv_type.setText("" + name);

        /**
         * 当前NFC的ID
         */
        String info = "";  //标签信息
        byte[] id = mTag.getId();
        info += ByteArrayToHexString(id) + "\n";
        tv_uid.setText("" + info);


        String result = "";
        result = readNdefTag(intent); // 这里只读取NDEF格式的数据
        tv_content.setText(result + "");
    }

    /**
     * step6:
     * NFC 卡中的命令可能有多种，需要两次循环读取所有写入的数据
     *
     * @param intent
     */
    private String readNdefTag(Intent intent) {
        Parcelable[] rawArray = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        try {
            String result = "";
            for (int i = 0; i < rawArray.length; i++) {
                NdefMessage mNdefMsg = (NdefMessage) rawArray[i];
                NdefRecord[] records = mNdefMsg.getRecords();

                String result2 = "";
                for (int j = 0; j < records.length; j++) {
                    NdefRecord mNdefRecord = mNdefMsg.getRecords()[j];
                    if (mNdefRecord != null) {
                        Log.d(TAG, "NdefMessage " + i + ",NdefRecord " + j + " :" + mNdefRecord.getPayload());
                        Log.d(TAG, "NdefMessage " + i + ",NdefRecord " + j + " :" + mNdefRecord.getPayload().length);
                        String content = new String(mNdefRecord.getPayload(), "UTF-8");
                        Log.d(TAG, "readNdefTag: " + content.trim() + "," + content.trim().length());
                        Log.d(TAG, "readNdefTag: " + content.getBytes().length);
                        Log.d(TAG, "readNdefTag: " + content + "," + content.length());
                        result2 += new String(mNdefRecord.getPayload(), "UTF-8") + ";";
                    }
                }
                result += result2 + "\n";
            }

            return result;

        } catch (Exception e) {
            Toast.makeText(this, e.getMessage().toString(), Toast.LENGTH_SHORT).show();
        }
        return "";
    }

}
