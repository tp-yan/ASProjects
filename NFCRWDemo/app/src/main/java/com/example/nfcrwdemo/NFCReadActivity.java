package com.example.nfctest;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
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

import static com.example.nfctest.Util.ByteArrayToHexString;


/**
 * 读卡信息 (继承baseNFC)
 * <p>
 * 大部分内容来自：CSDN博主「未曾远去」的原创文章
 * <p>
 * 版权声明：本文为CSDN博主「未曾远去」的原创文章，遵循 CC 4.0 BY-SA 版权协议，转载请附上原文出处链接及本声明。
 * 原文链接：https://blog.csdn.net/qq_36135335/article/details/82463179
 */
public class NFCReadActivity extends BaseNFCActivity {

    private static String TAG = "NFCReadActivity.class";

    private TextView tv_geshi; //格式

    private TextView tv_uid; //标签的id
    private TextView tv_jilv; //yue
    private TextView tv_content; //yue
    private TextView tv_type; //yue

    private RadioGroup radioGroup;

    private String[] type_list = new String[]{"IsoDep", "NfcA", "NfcB", "NfcF", "NfcV", "Ndef", "NdefFormatable", "MifareUltralight", "MifareClassic"};

    private String NFC_type = "Ndef";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc);

        initView();
    }

    private void initView() {

        tv_geshi = findViewById(R.id.geshi);
        tv_uid = findViewById(R.id.uid);
        radioGroup = findViewById(R.id.radio_group);
        tv_content = findViewById(R.id.content);
        tv_type = findViewById(R.id.type);

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


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Tag mTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        String[] techList = mTag.getTechList();

        //公共方法：
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
        switch (NFC_type) {
            case "IsoDep":
                result = ReadIsoDepTag(mTag);
                break;
            case "NfcA":
                result = readNfcATag(mTag);
                break;
            case "NfcB":
                result = readNfcBTag(mTag);
                break;
            case "NfcF":
                result = readNfcFTag(mTag);
                break;
            case "NfcV":
                result = readNfcVTag(mTag);
                break;
            case "Ndef":
                result = readNdefTag(intent);
                break;
            case "NdefFormatable":
                break;
            case "MifareUltralight":
                result = readUltralightTag(mTag);
                break;
            case "MifareClassic":
                result = readMifareClassicTag(mTag);
                break;
        }

        tv_content.setText(result + "");
    }


    private int bCount;
    private int bIndex;


    /**
     * Step1:IsoDep标签格式读取
     * MIFARE DESFire数据格式是IsoDep
     * 就是各种交通卡像武汉通，羊城通，深圳通，北京市政交通卡，长安通
     */
    // IsoDep读取数据
    public String ReadIsoDepTag(Tag tag) {
        IsoDep isoDep = IsoDep.get(tag);
        String str = "";
        try {
            isoDep.connect(); // 连接
            if (isoDep.isConnected()) {
                Log.i("ReadIsoDepTag", "isoDep.isConnected"); // 判断是否连接上
                // 1.select PSF (1PAY.SYS.DDF01)
                // 选择支付系统文件，它的名字是1PAY.SYS.DDF01。
                byte[] DFN_PSE = {(byte) '1', (byte) 'P', (byte) 'A', (byte) 'Y', (byte) '.', (byte) 'S', (byte) 'Y', (byte) 'S', (byte) '.', (byte) 'D', (byte) 'D', (byte) 'F', (byte) '0', (byte) '1',};
                isoDep.transceive(getSelectCommand(DFN_PSE));
                // 2.选择公交卡应用的名称
                byte[] DFN_SRV = {(byte) 0xA0, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x03, (byte) 0x86, (byte) 0x98, (byte) 0x07, (byte) 0x01,};
                isoDep.transceive(getSelectCommand(DFN_SRV));
                // 3.读取余额
                byte[] ReadMoney = {(byte) 0x80, // CLA Class
                        (byte) 0x5C, // INS Instruction
                        (byte) 0x00, // P1 Parameter 1
                        (byte) 0x02, // P2 Parameter 2
                        (byte) 0x04, // Le
                };
                byte[] Money = isoDep.transceive(ReadMoney);

                if (Money != null && Money.length > 4) {
                    int cash = byteToInt(Money, 4);
                    float ba = cash / 100.0f;

                }
                // 4.读取所有交易记录
                byte[] ReadRecord = {(byte) 0x00, // CLA Class
                        (byte) 0xB2, // INS Instruction
                        (byte) 0x01, // P1 Parameter 1
                        (byte) 0xC5, // P2 Parameter 2
                        (byte) 0x00, // Le
                };
                byte[] Records = isoDep.transceive(ReadRecord);
                // 处理Record

                tv_content.setText("总消费记录" + Records);

                Log.d("h_bl", "总消费记录" + Records);

                ArrayList<byte[]> ret = parseRecords(Records);

                List<String> retList = parseRecordsToStrings(ret);


            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (isoDep != null) {
                try {
                    isoDep.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


//
//        IsoDep isodep = IsoDep.get(tag);
//        try {
//            isodep.connect();
//            //select the card manager applet
//            byte[] mf = { (byte) '1', (byte) 'P',
//                    (byte) 'A', (byte) 'Y', (byte) '.', (byte) 'S', (byte) 'Y',
//                    (byte) 'S', (byte) '.', (byte) 'D', (byte) 'D', (byte) 'F',
//                    (byte) '0', (byte) '1', };
//            String result="";
//            byte[] mfRsp = isodep.transceive(getSelectCommand(mf));
//
//          //  Log.d(TAG, "mfRsp:" + HexToString(mfRsp));
//            //select Main Application
//            byte[] wht = { (byte) 0x41, (byte) 0x50,
//                    //此处以武汉通为例，其它的卡片参考对应的命令，网上可以查到
//                    (byte) 0x31, (byte) 0x2E, (byte) 0x57, (byte) 0x48, (byte) 0x43,
//                    (byte) 0x54, (byte) 0x43, };
//            byte[] sztRsp = isodep.transceive(getSelectCommand(wht));
//            byte[] balance = { (byte) 0x80, (byte) 0x5C, 0x00, 0x02, 0x04};
//            byte[] balanceRsp = isodep.transceive(balance);
//            //Log.d(TAG, "balanceRsp:" + HexToString(balanceRsp));
//            if(balanceRsp!=null && balanceRsp.length>4)
//            {
//                int cash = byteToInt(balanceRsp, 4);
//                float ba = cash / 100.0f;
//                result+="  余额："+String.valueOf(ba);
//
//            }
//            //  setNoteBody(result);
//            isodep.close();
//
//            return result;
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }


        return null;
    }

    /**
     * Step2:读取NfcA格式标签
     *
     * @param tag
     * @return
     */
    public String readNfcATag(Tag tag) {
        NfcA nfca = NfcA.get(tag);
        try {
            nfca.connect();
            if (nfca.isConnected()) {//NTAG216的芯片
                byte[] SELECT = {
                        (byte) 0x30,
                        (byte) 5 & 0x0ff,//0x05
                };
                byte[] response = nfca.transceive(SELECT);
                nfca.close();
                if (response != null) {
                    String s = new String(response, Charset.forName("utf-8"));
                    return s;
                }
            }
        } catch (Exception e) {

        }
        return null;
    }

    /**
     * step3: NfcB格式 一般用于身份证信息识别
     *
     * @param
     */
    private String readNfcBTag(Tag tag) {
        NfcB nfcbTag = NfcB.get(tag);
        try {
            nfcbTag.connect();
            if (nfcbTag.isConnected()) {

                Toast.makeText(this, "身份证已连接", Toast.LENGTH_SHORT).show();

                return "身份证已连接";
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (nfcbTag != null) {
                try {
                    nfcbTag.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error closing tag..身份证.", e);
                }
            }
        }

        return null;
    }

    /**
     * step4: NfcF 格式 数据读取
     *
     * @param mTag
     */
    private String readNfcFTag(Tag mTag) {
        NfcF nfc = NfcF.get(mTag);
        try {
            nfc.connect();
            byte[] felicaIDm = new byte[]{0};

//            byte[] req = readWithoutEncryption(felicaIDm, 10);
//            byte[] res = nfc.transceive(req);
//            nfc.close();
//            setNoteBody(ByteArrayToHexString(res));

        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        } finally {
            if (null != nfc) {
                try {
                    nfc.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }

    /**
     * step5: NfcV 格式
     *
     * @param mTag
     * @return
     */
    private String readNfcVTag(Tag mTag) {
        NfcV tech = NfcV.get(mTag);
        if (tech != null) {
            try {
                tech.connect();
                if (tech.isConnected()) {
                    byte[] tagUid = mTag.getId();
                    int blockAddress = 0;
                    int blocknum = 4;
                    byte[] cmd = new byte[]{
                            (byte) 0x22,  // FLAGS
                            (byte) 0x23,  // 20-READ_SINGLE_BLOCK,23-所有块
                            0, 0, 0, 0, 0, 0, 0, 0,
                            (byte) (blockAddress & 0x0ff), (byte) (blocknum - 1 & 0x0ff)
                    };
                    System.arraycopy(tagUid, 0, cmd, 2, tagUid.length);
                    byte[] response = tech.transceive(cmd);
                    tech.close();
                    if (response != null) {
                        // setNoteBody(new String(response, Charset.forName("utf-8")));
                    }
                }
            } catch (IOException e) {

            }
        }
        return null;
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

    /**
     * step:8 Ultralight格式读取
     *
     * @param tag
     * @return
     */
    public String readUltralightTag(Tag tag) {

//        MifareUltralight mifare = MifareUltralight.get(tag);
//        try {
//            mifare.connect();
//            int size = mifare.PAGE_SIZE;
//            byte[] payload = mifare.readPages(0);
//            String result = "page1：" + ByteArrayToHexString(payload) + "\n" + "总容量：" + String.valueOf(size) + "\n";
//            //这里只读取了其中几个page
//            byte[] payload1 = mifare.readPages(4);
//            byte[] payload2 = mifare.readPages(8);
//            byte[] payload3 = mifare.readPages(12);
//            result += "page4:" + ByteArrayToHexString(payload1) + "\npage8:" + ByteArrayToHexString(payload2) + "\npage12：" + ByteArrayToHexString(payload3) + "\n";
//            //byte[] payload4 = mifare.readPages(16);
//            //byte[] payload5 = mifare.readPages(20);
//            return result;
//            //+ new String(payload4, Charset.forName("US-ASCII"));
//            //+ new String(payload5, Charset.forName("US-ASCII"));
//        } catch (IOException e) {
//            Log.e(TAG, "IOException while writing MifareUltralight message...",
//                    e);
//            return "读取失败！";
//        } catch (Exception ee) {
//            Log.e(TAG, "IOException while writing MifareUltralight message...",
//                    ee);
//            return "读取失败！";
//        } finally {
//            if (mifare != null) {
//                try {
//                    mifare.close();
//                } catch (IOException e) {
//                    Log.e(TAG, "Error closing tag...", e);
//                }
//            }
//        }


        MifareUltralight ultralight = MifareUltralight.get(tag);
        try {
            ultralight.connect();
            byte[] data = ultralight.readPages(4);
            return new String(data, Charset.forName("GB2312"));
        } catch (Exception e) {
        } finally {
            try {
                ultralight.close();
            } catch (Exception e) {
            }
        }

        return null;
    }

    /**
     * step9:
     * 读取MifareClassic格式标签数据
     *
     * @param tag
     * @return
     */
    public String readMifareClassicTag(Tag tag) {
        MifareClassic mfc = MifareClassic.get(tag);
        for (String tech : tag.getTechList()) {
            System.out.println(tech);// 显示设备支持技术
        }
        boolean auth = false;
        // 读取TAG

        try {
            // metaInfo.delete(0, metaInfo.length());//清空StringBuilder;
            StringBuilder metaInfo = new StringBuilder();
            // Enable I/O operations to the tag from this TagTechnology object.
            mfc.connect();
            int type = mfc.getType();// 获取TAG的类型
            int sectorCount = mfc.getSectorCount();// 获取TAG中包含的扇区数
            String typeS = "";
            switch (type) {
                case MifareClassic.TYPE_CLASSIC:
                    typeS = "TYPE_CLASSIC";
                    break;
                case MifareClassic.TYPE_PLUS:
                    typeS = "TYPE_PLUS";
                    break;
                case MifareClassic.TYPE_PRO:
                    typeS = "TYPE_PRO";
                    break;
                case MifareClassic.TYPE_UNKNOWN:
                    typeS = "TYPE_UNKNOWN";
                    break;

            }
            metaInfo.append("  卡片类型：" + typeS + "\n共" + sectorCount + "个扇区\n共"
                    + mfc.getBlockCount() + "个块\n存储空间: " + mfc.getSize()
                    + "B\n");
            for (int j = 0; j < sectorCount; j++) {
                // Authenticate a sector with key A.
                auth = mfc.authenticateSectorWithKeyA(j,
                        MifareClassic.KEY_NFC_FORUM);// 逐个获取密码
                /**
                 * byte[]
                 * codeByte_Default=MifareClassic.KEY_DEFAULT;//FFFFFFFFFFFF
                 * byte[]
                 * codeByte_Directory=MifareClassic.KEY_MIFARE_APPLICATION_DIRECTORY
                 * ;//A0A1A2A3A4A5 byte[]
                 * codeByte_Forum=MifareClassic.KEY_NFC_FORUM;//D3F7D3F7D3F7
                 */
                if (auth) {
                    metaInfo.append("Sector " + j + ":验证成功\n");
                    // 读取扇区中的块

                    bCount = mfc.getBlockCountInSector(j);
                    bIndex = mfc.sectorToBlock(j);
                    for (int i = 0; i < bCount; i++) {
                        byte[] data = mfc.readBlock(bIndex);
                        metaInfo.append("Block " + bIndex + " : "
                                + ByteArrayToHexString(data)
                                + "\n");
                        bIndex++;
                    }
                } else {
                    metaInfo.append("Sector " + j + ":验证失败\n");
                }
            }
            return metaInfo.toString();

        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        } finally {
            if (mfc != null) {
                try {
                    mfc.close();
                } catch (IOException e) {
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG)
                            .show();
                }
            }
        }
        return null;
    }


//----------------------------------------------------------------------------------
//-------------------------------------------工具方法和类--------------------------------
//----------------------------------------------------------------------------------


    private byte[] getSelectCommand(byte[] aid) {
        final ByteBuffer cmd_pse = ByteBuffer.allocate(aid.length + 6);
        cmd_pse.put((byte) 0x00) // CLA Class
                .put((byte) 0xA4) // INS Instruction
                .put((byte) 0x04) // P1 Parameter 1
                .put((byte) 0x00) // P2 Parameter 2
                .put((byte) aid.length) // Lc
                .put(aid).put((byte) 0x00); // Le
        return cmd_pse.array();
    }


    private ArrayList<byte[]> parseRecords(byte[] Records) {
        int max = Records.length / 23;
        Log.d("h_bl", "消费记录有" + max + "条");
        ArrayList<byte[]> ret = new ArrayList<byte[]>();
        for (int i = 0; i < max; i++) {
            byte[] aRecord = new byte[23];
            for (int j = 23 * i, k = 0; j < 23 * (i + 1); j++, k++) {
                aRecord[k] = Records[j];
            }
            ret.add(aRecord);
        }
        for (byte[] bs : ret) {
            Log.d("h_bl", "消费记录有byte[]" + bs); // 有数据。解析正确。
        }
        return ret;
    }

    protected final static byte TRANS_CSU = 6; // 如果等于0x06或者0x09，表示刷卡；否则是充值
    protected final static byte TRANS_CSU_CPX = 9; // 如果等于0x06或者0x09，表示刷卡；否则是充值

    private List<String> parseRecordsToStrings(ArrayList<byte[]>... Records) {
        List<String> recordsList = new ArrayList<String>();
        for (ArrayList<byte[]> record : Records) {
            if (record == null)
                continue;
            for (byte[] v : record) {
                StringBuilder r = new StringBuilder();
                int cash = toInt(v, 5, 4);


                char t = (v[9] == TRANS_CSU || v[9] == TRANS_CSU_CPX) ? '-' : '+';
                r.append(String.format("%02X%02X.%02X.%02X %02X:%02X ", v[16], v[17], v[18], v[19], v[20], v[21], v[22]));
                r.append("   " + t).append((cash / 100.0f) + "");
                String aLog = r.toString();
                recordsList.add(aLog);
            }
        }
        return recordsList;
    }

    public static int toInt(byte[] b, int i, int y) {
        return b[3] & 0xFF | (b[2] & 0xFF) << 8 | (b[1] & 0xFF) << 16 | (b[0] & 0xFF) << 24;
    }

    //将byte 转成int
    private int byteToInt(byte[] b, int n) {
        int ret = 0;
        for (int i = 0; i < n; i++) {
            ret = ret << 8;
            ret |= b[i] & 0x00FF;
        }
        if (ret > 100000 || ret < -100000)
            ret -= 0x80000000;
        return ret;
    }

}
