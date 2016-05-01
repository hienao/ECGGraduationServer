package cn.edu.tjut.ecg.ecgserver.utils;

import android.content.Context;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cn.edu.tjut.ecg.ecgserver.common.RPeakDetection;
import cn.edu.tjut.ecg.ecgserver.filter.DigitalFilter;
import cn.edu.tjut.ecg.ecgserver.filter.LowPassFilter;

/**
 * 从文件中读取波形数据的类
 * Created by Administrator on 2016/3/16 0016.
 */
public class GetWaveDataFromFile {
    public GetWaveDataFromFile(Context context) {
       this.context=context;
    }
    private Context context;
    private String filepath;
    private float[] rPeak = null;
    private static final int StartFlag = 0xFC;
    private static final int EndFlag = 0xFD;
    private static final int EscapeFlag = 0xEF;
    private static final int EscapeValue = 0x20;
    private static final float saticDistance = 110;
    float dataMax, dataMin = 0;

    public long getTimeFromPath(String filepath){
        int start=filepath.lastIndexOf('_')+1;
        int end=filepath.lastIndexOf('.')-1;
        String timestr=filepath.substring(start,end);
        return Long.parseLong(timestr);
    }
    public float[]getDataRPeak(float[]DataFiltered)
    {
        float[] rPeak = null;
        RPeakDetection mRPeakDetection = new RPeakDetection();
        rPeak = mRPeakDetection.RPeakRecognize(DataFiltered);//获取R点值
        return rPeak;
    }
    /**
     * 读取数据
     *
     * @return
     */
    private double[] getECGData (String path){

        ArrayList<Byte> dataBufList = new ArrayList<Byte>();
        List<Float> dataList = new ArrayList<Float>();
        filepath = path;

        try {
            FileInputStream fin = new FileInputStream(filepath);
            int length = fin.available();
            byte[] buffer = new byte[length];
            fin.read(buffer);
            fin.close();
            //i为读取字节下标
            int i = 0;
            /*
             * 利用"##"最为标记位，将信息数据与心电数据分析
			 * "#"ASCII码十六进制表示为0x23
			 * 此版本没有读取个人信息
			 */
            for (; i < buffer.length; i++) {
                if (((buffer[i] & 0xFF) == 0x23) && ((buffer[i + 1] & 0xFF) == 0x23))
                    break;
            }
            i += 2;
            for (; i < buffer.length; i++)
                if ((buffer[i] & 0xFF) == StartFlag)
                    break;
            for (; i < buffer.length; i++) {
                if ((buffer[i] & 0xFF) == StartFlag) {
                    continue;
                } else if ((buffer[i] & 0xFF) == EndFlag) {
                    if (dataBufList.size() < 10)
                        if (dataBufList.size() == 3) {
                            if ((dataBufList.get(0)) > 0) {
                                dataList.add((float) ((dataBufList.get(0) & 0xFF) << 16 | (dataBufList.get(1) & 0xFF) << 8 | (dataBufList.get(2) & 0xFF)));
                            } else {
                                dataList.add((float) ((0xff << 24) | (dataBufList.get(0) & 0xFF) << 16 | (dataBufList.get(1) & 0xFF) << 8 | (dataBufList.get(2) & 0xFF)));
                            }

                            dataBufList.clear();
                        }
                    continue;
                } else {
                    if ((buffer[i] & 0xFF) == EscapeFlag) {
                        dataBufList.add((byte) ((buffer[i++] & 0xFF) ^ EscapeValue));
                    } else {
                        dataBufList.add(buffer[i]);
                    }
                }
            }
            buffer = null;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        double[] data = new double[dataList.size()];
        for (int j = 0; j < dataList.size(); j++) {
            data[j] = dataList.get(j);
        }
        //		Log.i("data.length", "-- "+data.length);
        //		data = deleteData(data,data.length-1000,data.length-1);
        //		Log.i("data.length", "-- "+data.length);
        dataBufList.clear();
        dataList.clear();
        return data;
    }
    /**
     * 数据滤波
     *
     * @param data
     * @return
     */
    private double[] filterWave ( double[] data){
        //数字滤波，去基线偏移
        double[] datatemp = new double[data.length];
        double k = 0.7;
        int samplerate = 250;
        double fc = 0.8 / samplerate;
        double[] a = new double[2];
        double[] b = new double[2];
        double alpha = (1 - k * Math.cos(2 * Math.PI * fc) - Math.sqrt(2 * k * (1 - Math.cos(2 * Math.PI * fc)) - Math.pow(k, 2) * Math.pow(Math.sin(2 * Math.PI * fc), 2))) / (1 - k);
        a[0] = 1 - alpha;
        a[1] = 0;
        b[0] = 1;
        b[1] = -1 * alpha;
        DigitalFilter digitalFilter = new DigitalFilter(a, b, data, 1d);
        datatemp = digitalFilter.zeroFilter();

        for (int i = 0; i < data.length; i++) {
            data[i] = data[i] - datatemp[i];
        }

        //低通滤波
        LowPassFilter lowPassFilter = new LowPassFilter();
        data = lowPassFilter.lvboFilter(data);
        //滑动平均滤波
        //		MovingAverageFilter movingAverageFilter = new MovingAverageFilter();
        //		data = movingAverageFilter.filter(data);

        return data;
    }
    }