package com.example.power_prediction.service.Impl;

import com.example.power_prediction.entity.Device;
import com.example.power_prediction.entity.PowerDistributionDay;
import com.example.power_prediction.repository.PowerDistributionDayRepository;
import com.example.power_prediction.repository.PowerDistributionHourRepository;
import com.example.power_prediction.service.PowerStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class ImplPowerStatisticsService implements PowerStatisticsService {
    @Autowired
    PowerDistributionDayRepository powerDistributionDayRepository;
    @Autowired
    PowerDistributionHourRepository powerDistributionHourRepository;


    @Override
    public List<Map> findMultipleDevicePowerMsg(String multipleId, Integer dataTime, Integer dayNum, Integer type) {
        String[] idList = multipleId.split(",");
//        List<Integer> idListNew = new ArrayList<>();
        List<Map> mapList = new ArrayList<>();
        for (String deviceId : idList) {
            List<Object[]> powerDistributionDays = powerDistributionDayRepository.findAllByDeviceAndDataTimeBetweenAsc(Integer.valueOf(deviceId), dataTime, dataTime + 86400 * dayNum - 1);
            if (powerDistributionDays.size() == 0) {
                break;
            }
            Map msgMap = new HashMap();
            msgMap.put("deviceName", powerDistributionDays.get(0)[0]);
            Integer i = 1;
            Double monthTotalKWh = 0.0;
            Double monthHighKWh = 0.0;
            Double monthLowKWh = 0.0;
            Double monthMidKWh = 0.0;
            Double monthTopKWh = 0.0;
            Double monthTotalCharge = 0.0;
            Double monthHighCharge = 0.0;
            Double monthLowCharge = 0.0;
            Double monthMidCharge = 0.0;
            Double monthTopCharge = 0.0;
//            Double monthTotalKWh1 = 0.0;
//            Double monthTotalCharge1 = 0.0;
//            Double monthTotalKWh2 = 0.0;
//            Double monthTotalCharge2 = 0.0;
//            Double monthTotalKWh3 = 0.0;
//            Double monthTotalCharge3 = 0.0;
//            Double monthTotalKWh4 = 0.0;
//            Double monthTotalCharge4 = 0.0;
//            Double monthTotalKWh5 = 0.0;
//            Double monthTotalCharge5 = 0.0;
//            Double monthTotalKWh6 = 0.0;
//            Double monthTotalCharge6 = 0.0;
//            Double monthTotalKWh7 = 0.0;
//            Double monthTotalCharge7 = 0.0;
//            Double monthTotalKWh8 = 0.0;
//            Double monthTotalCharge8 = 0.0;
//            Double monthTotalKWh9 = 0.0;
//            Double monthTotalCharge9 = 0.0;
//            Double monthTotalKWh10 = 0.0;
//            Double monthTotalCharge10 = 0.0;
//            Double monthTotalKWh11 = 0.0;
//            Double monthTotalCharge11 = 0.0;
//            Double monthTotalKWh12 = 0.0;
//            Double monthTotalCharge12 = 0.0;
            Double[] monthCharge = new Double[12];
            Arrays.fill(monthCharge, 0.0);
            Double[] monthKWh = new Double[12];
            Arrays.fill(monthKWh, 0.0);
            Double[] monthTotalHighKWh = new Double[12];
            Arrays.fill(monthTotalHighKWh, 0.0);
            Double[] monthTotalMidKWh = new Double[12];
            Arrays.fill(monthTotalMidKWh, 0.0);
            Double[] monthTotalLowKWh = new Double[12];
            Arrays.fill(monthTotalLowKWh, 0.0);
            Double[] monthTotalTopKWh = new Double[12];
            Arrays.fill(monthTotalTopKWh, 0.0);
            Double[] monthTotalHighCharge = new Double[12];
            Arrays.fill(monthTotalHighCharge, 0.0);
            Double[] monthTotalMidCharge = new Double[12];
            Arrays.fill(monthTotalMidCharge, 0.0);
            Double[] monthTotalLowCharge = new Double[12];
            Arrays.fill(monthTotalLowCharge, 0.0);
            Double[] monthTotalTopCharge = new Double[12];
            Arrays.fill(monthTotalTopCharge, 0.0);


            for (Object[] o : powerDistributionDays) {
                monthTotalKWh = Double.valueOf(String.valueOf(o[5])) + monthTotalKWh;
                monthHighKWh = Double.valueOf(String.valueOf(o[6])) + monthHighKWh;
                monthLowKWh = Double.valueOf(String.valueOf(o[7])) + monthLowKWh;
                monthMidKWh = Double.valueOf(String.valueOf(o[8])) + monthMidKWh;
                monthTopKWh = Double.valueOf(String.valueOf(o[9])) + monthTopKWh;
                monthTotalCharge = Double.valueOf(String.valueOf(o[10])) + monthTotalCharge;
                monthHighCharge = Double.valueOf(String.valueOf(o[11])) + monthHighCharge;
                monthLowCharge = Double.valueOf(String.valueOf(o[12])) + monthLowCharge;
                monthMidCharge = Double.valueOf(String.valueOf(o[13])) + monthMidCharge;
                monthTopCharge = Double.valueOf(String.valueOf(o[14])) + monthTopCharge;


                if (type == 1) {   //type选择为1表示适用于月峰谷平尖
                    msgMap.put(i + "totalLoad", o[4]);
                    msgMap.put(i + "haototal", o[5]);
                    msgMap.put(i + "haof", o[6]);
                    msgMap.put(i + "haog", o[7]);
                    msgMap.put(i + "haop", o[8]);
                    msgMap.put(i + "haoj", o[9]);
                    msgMap.put("totalCharge" + i, o[10]);
                    msgMap.put("highCharge" + i, o[11]);
                    msgMap.put("lowCharge" + i, o[12]);
                    msgMap.put("midCharge" + i, o[13]);
                    msgMap.put("topCharge" + i, o[14]);
                    msgMap.put("Quantity" + i + "r", o[5]);
                    i++;
                }

                if (type == 2)//type选择为2表示适用于年电量分析
                {
//                    for (int key = 1; key <= 12; key++) {
//                        Double monthTotalKWh1key = 0.0;
//                    }

                    Long timestamp = Long.valueOf(Integer.valueOf(String.valueOf(o[3])));
//                    Long timeStamp = Long.valueOf(timestamp * 1000);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String sd = sdf.format(new Date(Long.parseLong(String.valueOf(timestamp * 1000L))));
//                    System.out.println(sd);
                    Integer month = Integer.valueOf(sd.substring(5, 7));
//                    System.out.println(month);
                    switch (month) {
                        case 1:
                            monthKWh[0] += Double.valueOf(String.valueOf(o[5]));
                            monthTotalHighKWh[0] += Double.valueOf(String.valueOf(o[6]));
                            monthTotalLowKWh[0] += Double.valueOf(String.valueOf(o[7]));
                            monthTotalMidKWh[0] += Double.valueOf(String.valueOf(o[8]));
                            monthTotalTopKWh[0] += Double.valueOf(String.valueOf(o[9]));
                            monthCharge[0] += Double.valueOf(String.valueOf(o[10]));
                            monthTotalHighCharge[0] += Double.valueOf(String.valueOf(o[11]));
                            monthTotalLowCharge[0] += Double.valueOf(String.valueOf(o[12]));
                            monthTotalMidCharge[0] += Double.valueOf(String.valueOf(o[13]));
                            monthTotalTopCharge[0] += Double.valueOf(String.valueOf(o[14]));
                            break;
                        case 2:
                            monthKWh[1] += Double.valueOf(String.valueOf(o[5]));
                            monthTotalHighKWh[1] += Double.valueOf(String.valueOf(o[6]));
                            monthTotalMidKWh[1] += Double.valueOf(String.valueOf(o[8]));
                            monthTotalLowKWh[1] += Double.valueOf(String.valueOf(o[7]));
                            monthTotalTopKWh[1] += Double.valueOf(String.valueOf(o[9]));
                            monthCharge[1] += Double.valueOf(String.valueOf(o[10]));
                            monthTotalHighCharge[1] += Double.valueOf(String.valueOf(o[11]));
                            monthTotalLowCharge[1] += Double.valueOf(String.valueOf(o[12]));
                            monthTotalMidCharge[1] += Double.valueOf(String.valueOf(o[13]));
                            monthTotalTopCharge[1] += Double.valueOf(String.valueOf(o[14]));
                            break;
                        case 3:
                            monthKWh[2] += Double.valueOf(String.valueOf(o[5]));
                            monthTotalHighKWh[2] += Double.valueOf(String.valueOf(o[6]));
                            monthTotalMidKWh[2] += Double.valueOf(String.valueOf(o[8]));
                            monthTotalLowKWh[2] += Double.valueOf(String.valueOf(o[7]));
                            monthTotalTopKWh[2] += Double.valueOf(String.valueOf(o[9]));
                            monthCharge[2] += Double.valueOf(String.valueOf(o[10]));
                            monthTotalHighCharge[2] += Double.valueOf(String.valueOf(o[11]));
                            monthTotalLowCharge[2] += Double.valueOf(String.valueOf(o[12]));
                            monthTotalMidCharge[2] += Double.valueOf(String.valueOf(o[13]));
                            monthTotalTopCharge[2] += Double.valueOf(String.valueOf(o[14]));
                            break;
                        case 4:
                            monthKWh[3] += Double.valueOf(String.valueOf(o[5]));
                            monthTotalHighKWh[3] += Double.valueOf(String.valueOf(o[6]));
                            monthTotalMidKWh[3] += Double.valueOf(String.valueOf(o[8]));
                            monthTotalLowKWh[3] += Double.valueOf(String.valueOf(o[7]));
                            monthTotalTopKWh[3] += Double.valueOf(String.valueOf(o[9]));
                            monthCharge[3] += Double.valueOf(String.valueOf(o[10]));
                            monthTotalHighCharge[3] += Double.valueOf(String.valueOf(o[11]));
                            monthTotalLowCharge[3] += Double.valueOf(String.valueOf(o[12]));
                            monthTotalMidCharge[3] += Double.valueOf(String.valueOf(o[13]));
                            monthTotalTopCharge[3] += Double.valueOf(String.valueOf(o[14]));
                            break;
                        case 5:
                            monthKWh[4] += Double.valueOf(String.valueOf(o[5]));
                            monthTotalHighKWh[4] += Double.valueOf(String.valueOf(o[6]));
                            monthTotalMidKWh[4] += Double.valueOf(String.valueOf(o[8]));
                            monthTotalLowKWh[4] += Double.valueOf(String.valueOf(o[7]));
                            monthTotalTopKWh[4] += Double.valueOf(String.valueOf(o[9]));
                            monthCharge[4] += Double.valueOf(String.valueOf(o[10]));
                            monthTotalHighCharge[4] += Double.valueOf(String.valueOf(o[11]));
                            monthTotalLowCharge[4] += Double.valueOf(String.valueOf(o[12]));
                            monthTotalMidCharge[4] += Double.valueOf(String.valueOf(o[13]));
                            monthTotalTopCharge[4] += Double.valueOf(String.valueOf(o[14]));
                            break;
                        case 6:
                            monthKWh[5] += Double.valueOf(String.valueOf(o[5]));
                            monthTotalHighKWh[5] += Double.valueOf(String.valueOf(o[6]));
                            monthTotalMidKWh[5] += Double.valueOf(String.valueOf(o[8]));
                            monthTotalLowKWh[5] += Double.valueOf(String.valueOf(o[7]));
                            monthTotalTopKWh[5] += Double.valueOf(String.valueOf(o[9]));
                            monthCharge[5] += Double.valueOf(String.valueOf(o[10]));
                            monthTotalHighCharge[5] += Double.valueOf(String.valueOf(o[11]));
                            monthTotalLowCharge[5] += Double.valueOf(String.valueOf(o[12]));
                            monthTotalMidCharge[5] += Double.valueOf(String.valueOf(o[13]));
                            monthTotalTopCharge[5] += Double.valueOf(String.valueOf(o[14]));
                            break;
                        case 7:
                            monthKWh[6] += Double.valueOf(String.valueOf(o[5]));
                            monthTotalHighKWh[6] += Double.valueOf(String.valueOf(o[6]));
                            monthTotalMidKWh[6] += Double.valueOf(String.valueOf(o[8]));
                            monthTotalLowKWh[6] += Double.valueOf(String.valueOf(o[7]));
                            monthTotalTopKWh[6] += Double.valueOf(String.valueOf(o[9]));
                            monthCharge[6] += Double.valueOf(String.valueOf(o[10]));
                            monthTotalHighCharge[6] += Double.valueOf(String.valueOf(o[11]));
                            monthTotalLowCharge[6] += Double.valueOf(String.valueOf(o[12]));
                            monthTotalMidCharge[6] += Double.valueOf(String.valueOf(o[13]));
                            monthTotalTopCharge[6] += Double.valueOf(String.valueOf(o[14]));
                            break;
                        case 8:
                            monthKWh[7] += Double.valueOf(String.valueOf(o[5]));
                            monthTotalHighKWh[7] += Double.valueOf(String.valueOf(o[6]));
                            monthTotalMidKWh[7] += Double.valueOf(String.valueOf(o[8]));
                            monthTotalLowKWh[7] += Double.valueOf(String.valueOf(o[7]));
                            monthTotalTopKWh[7] += Double.valueOf(String.valueOf(o[9]));
                            monthCharge[7] += Double.valueOf(String.valueOf(o[10]));
                            monthTotalHighCharge[7] += Double.valueOf(String.valueOf(o[11]));
                            monthTotalLowCharge[7] += Double.valueOf(String.valueOf(o[12]));
                            monthTotalMidCharge[7] += Double.valueOf(String.valueOf(o[13]));
                            monthTotalTopCharge[7] += Double.valueOf(String.valueOf(o[14]));
                            break;
                        case 9:
                            monthKWh[8] += Double.valueOf(String.valueOf(o[5]));
                            monthTotalHighKWh[8] += Double.valueOf(String.valueOf(o[6]));
                            monthTotalMidKWh[8] += Double.valueOf(String.valueOf(o[8]));
                            monthTotalLowKWh[8] += Double.valueOf(String.valueOf(o[7]));
                            monthTotalTopKWh[8] += Double.valueOf(String.valueOf(o[9]));
                            monthCharge[8] += Double.valueOf(String.valueOf(o[10]));
                            monthTotalHighCharge[8] += Double.valueOf(String.valueOf(o[11]));
                            monthTotalLowCharge[8] += Double.valueOf(String.valueOf(o[12]));
                            monthTotalMidCharge[8] += Double.valueOf(String.valueOf(o[13]));
                            monthTotalTopCharge[8] += Double.valueOf(String.valueOf(o[14]));
                            break;
                        case 10:
                            monthKWh[9] += Double.valueOf(String.valueOf(o[5]));
                            monthTotalHighKWh[9] += Double.valueOf(String.valueOf(o[6]));
                            monthTotalMidKWh[9] += Double.valueOf(String.valueOf(o[8]));
                            monthTotalLowKWh[9] += Double.valueOf(String.valueOf(o[7]));
                            monthTotalTopKWh[9] += Double.valueOf(String.valueOf(o[9]));
                            monthCharge[9] += Double.valueOf(String.valueOf(o[10]));
                            monthTotalHighCharge[9] += Double.valueOf(String.valueOf(o[11]));
                            monthTotalLowCharge[9] += Double.valueOf(String.valueOf(o[12]));
                            monthTotalMidCharge[9] += Double.valueOf(String.valueOf(o[13]));
                            monthTotalTopCharge[9] += Double.valueOf(String.valueOf(o[14]));
                            break;
                        case 11:
                            monthKWh[10] += Double.valueOf(String.valueOf(o[5]));
                            monthTotalHighKWh[10] += Double.valueOf(String.valueOf(o[6]));
                            monthTotalMidKWh[10] += Double.valueOf(String.valueOf(o[8]));
                            monthTotalLowKWh[10] += Double.valueOf(String.valueOf(o[7]));
                            monthTotalTopKWh[10] += Double.valueOf(String.valueOf(o[9]));
                            monthCharge[10] += Double.valueOf(String.valueOf(o[10]));
                            monthTotalHighCharge[10] += Double.valueOf(String.valueOf(o[11]));
                            monthTotalLowCharge[10] += Double.valueOf(String.valueOf(o[12]));
                            monthTotalMidCharge[10] += Double.valueOf(String.valueOf(o[13]));
                            monthTotalTopCharge[10] += Double.valueOf(String.valueOf(o[14]));
                            break;
                        case 12:
                            monthKWh[11] += Double.valueOf(String.valueOf(o[5]));
                            monthTotalHighKWh[11] += Double.valueOf(String.valueOf(o[6]));
                            monthTotalMidKWh[11] += Double.valueOf(String.valueOf(o[8]));
                            monthTotalLowKWh[11] += Double.valueOf(String.valueOf(o[7]));
                            monthTotalTopKWh[11] += Double.valueOf(String.valueOf(o[9]));
                            monthCharge[11] += Double.valueOf(String.valueOf(o[10]));
                            monthTotalHighCharge[11] += Double.valueOf(String.valueOf(o[11]));
                            monthTotalLowCharge[11] += Double.valueOf(String.valueOf(o[12]));
                            monthTotalMidCharge[11] += Double.valueOf(String.valueOf(o[13]));
                            monthTotalTopCharge[11] += Double.valueOf(String.valueOf(o[14]));
                            break;
                    }

                }

            }
            if (type == 2) {
                ArrayList<Map> arrayList = new ArrayList();
                for (int key = 0; key < 12; key++) {
                    Map map = new HashMap();
                    map.put("monthTotalKWh", String.format("%.2f", monthKWh[key]));
                    map.put("monthHighKWh", String.format("%.2f", monthTotalHighKWh[key]));
                    map.put("monthLowKWh", String.format("%.2f", monthTotalLowKWh[key]));
                    map.put("monthMidKWh", String.format("%.2f", monthTotalMidKWh[key]));
                    map.put("monthTopKWh", String.format("%.2f", monthTotalTopKWh[key]));
                    map.put("monthTotalCharge", String.format("%.2f", monthCharge[key]));
                    map.put("monthHighCharge", String.format("%.2f", monthTotalHighCharge[key]));
                    map.put("monthLowCharge", String.format("%.2f", monthTotalLowCharge[key]));
                    map.put("monthMidCharge", String.format("%.2f", monthTotalMidCharge[key]));
                    map.put("monthTopCharge", String.format("%.2f", monthTotalTopCharge[key]));
                    msgMap.put("monthTotalKWh" + (key + 1), String.format("%.2f", monthKWh[key]));
                    msgMap.put("monthTotalCharge" + (key + 1), String.format("%.2f", (monthCharge[key])));

                    arrayList.add(map);
                }
                msgMap.put("msgList", arrayList);
//
//                msgMap.put("monthTotalKWh1", String.format("%.2f", (monthTotalKWh1)));
//                msgMap.put("monthTotalKWh2", String.format("%.2f", (monthTotalKWh2)));
//                msgMap.put("monthTotalKWh3", String.format("%.2f", (monthTotalKWh3)));
//                msgMap.put("monthTotalKWh4", String.format("%.2f", (monthTotalKWh4)));
//                msgMap.put("monthTotalKWh5", String.format("%.2f", (monthTotalKWh5)));
//                msgMap.put("monthTotalKWh6", String.format("%.2f", (monthTotalKWh6)));
//                msgMap.put("monthTotalKWh7", String.format("%.2f", (monthTotalKWh7)));
//                msgMap.put("monthTotalKWh8", String.format("%.2f", (monthTotalKWh8)));
//                msgMap.put("monthTotalKWh9", String.format("%.2f", (monthTotalKWh9)));
//                msgMap.put("monthTotalKWh10", String.format("%.2f", (monthTotalKWh10)));
//                msgMap.put("monthTotalKWh11", String.format("%.2f", (monthTotalKWh11)));
//                msgMap.put("monthTotalKWh12", String.format("%.2f", (monthTotalKWh12)));
//
//                msgMap.put("monthTotalCharge1", String.format("%.2f", (monthTotalCharge1)));
//                msgMap.put("monthTotalCharge2", String.format("%.2f", (monthTotalCharge2)));
//                msgMap.put("monthTotalCharge3", String.format("%.2f", (monthTotalCharge3)));
//                msgMap.put("monthTotalCharge4", String.format("%.2f", (monthTotalCharge4)));
//                msgMap.put("monthTotalCharge5", String.format("%.2f", (monthTotalCharge5)));
//                msgMap.put("monthTotalCharge6", String.format("%.2f", (monthTotalCharge6)));
//                msgMap.put("monthTotalCharge7", String.format("%.2f", (monthTotalCharge7)));
//                msgMap.put("monthTotalCharge8", String.format("%.2f", (monthTotalCharge8)));
//                msgMap.put("monthTotalCharge9", String.format("%.2f", (monthTotalCharge9)));
//                msgMap.put("monthTotalCharge10", String.format("%.2f", (monthTotalCharge10)));
//                msgMap.put("monthTotalCharge11", String.format("%.2f", (monthTotalCharge11)));
//                msgMap.put("monthTotalCharge12", String.format("%.2f", (monthTotalCharge12)));
            }
            msgMap.put("monthTotalKWh", String.format("%.2f", (monthTotalKWh)));
            msgMap.put("monthHighKWh", String.format("%.2f", (monthHighKWh)));
            msgMap.put("monthLowKWh", String.format("%.2f", (monthLowKWh)));
            msgMap.put("monthMidKWh", String.format("%.2f", (monthMidKWh)));
            msgMap.put("monthTopKWh", String.format("%.2f", (monthTopKWh)));
            msgMap.put("monthTotalCharge", String.format("%.2f", (monthTotalCharge)));
            msgMap.put("monthHighCharge", String.format("%.2f", (monthHighCharge)));
            msgMap.put("monthLowCharge", String.format("%.2f", (monthLowCharge)));
            msgMap.put("monthMidCharge", String.format("%.2f", (monthMidCharge)));
            msgMap.put("monthTopCharge", String.format("%.2f", (monthTopCharge)));
            if (type == 3) { //type选择为3表示适用于电量电费分析
                if (monthTotalKWh != 0) {
                    msgMap.put("monthHighKWhPercentage", String.format("%.2f", (monthHighKWh / monthTotalKWh)));
                    msgMap.put("monthLowKWhPercentage", String.format("%.2f", (monthLowKWh / monthTotalKWh)));
                    msgMap.put("monthMidKWhPercentage", String.format("%.2f", (monthMidKWh / monthTotalKWh)));
                    msgMap.put("monthTopKWhPercentage", String.format("%.2f", (monthTopKWh / monthTotalKWh)));
                }
                if (monthTotalCharge != 0) {
                    msgMap.put("monthHighChargePercentage", String.format("%.2f", (monthHighCharge / monthTotalCharge)));
                    msgMap.put("monthLowChargePercentage", String.format("%.2f", (monthLowCharge / monthTotalCharge)));
                    msgMap.put("monthMidChargePercentage", String.format("%.2f", (monthMidCharge / monthTotalCharge)));
                    msgMap.put("monthTopChargePercentage", String.format("%.2f", (monthTopCharge / monthTotalCharge)));
                }
            }
            mapList.add(msgMap);
        }
        return mapList;
    }

    @Override
    public List<Map> findMultipleDevicePowerMsgForDay(String multipleId, Integer start, Integer end) {

        String[] idList = multipleId.split(",");
//        List<Integer> idListNew = new ArrayList<>();
        List<Map> mapList = new ArrayList<>();
        for (String deviceId : idList) {
            List<Object[]> powerDistributionHours = powerDistributionHourRepository.findAllByDeviceAndDataTimeBetweenAsc(Integer.valueOf(deviceId), start, end);
            if (powerDistributionHours.size() == 0) {
                break;
            }
            Map hourMap = new HashMap();
            if (powerDistributionHours.get(0)[0] != null)
                hourMap.put("deviceName", powerDistributionHours.get(0)[0]);
            else continue;
            if (powerDistributionHours.get(0)[1] == null) {
                hourMap.put("totalKWh", 0);
            } else {
                hourMap.put("totalKWh", String.format("%.2f", powerDistributionHours.get(0)[1]));
            }
            mapList.add(hourMap);
        }
        return mapList;
    }
}

