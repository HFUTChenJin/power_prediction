package com.example.power_prediction.service.Impl;

import com.example.power_prediction.entity.*;
import com.example.power_prediction.repository.*;
import com.example.power_prediction.service.PowerAnalyseMonthService;
import com.example.power_prediction.service.PowerBillByDayService;
import com.example.power_prediction.service.PowerPriceTimeService;
import com.example.power_prediction.service.UtilService;
import com.example.power_prediction.util.TimeOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

@Service
public class ImplPowerBillByDayService implements PowerBillByDayService {
    @Autowired
    PowerBillByDayRepository powerBillByDayRepository;

    @Autowired
    PowerRealtimeRepository powerRealtimeRepository;

    @Autowired
    PowerPriceTimeService powerPriceTimeService;

    @Autowired
    DeviceRelationshipRepository deviceRelationshipRepository;

    @Autowired
    UtilService utilService;

    @Autowired
    DeviceRepository deviceRepository;

    @Autowired
    PowerAnalyseMonthService powerAnalyseMonthService;


    /**
     * 判断该时间处在哪个计时段
     *
     * @param powerPriceTime getDevicePowerPriceInTime得到的Map对象
     * @param time           时间
     * @return “f”“g”“p”“j”
     */
    private String getTimeZone(Map<String, Object> powerPriceTime, LocalTime time) {
        String[] keys = {"f", "g", "p", "j"};
        for (String key : keys) {
            Map<String, Object> timeAndPrice = (Map<String, Object>) powerPriceTime.get(key);
            List<LocalTime> starts = (List<LocalTime>) timeAndPrice.get("start");
            List<LocalTime> ends = (List<LocalTime>) timeAndPrice.get("end");
            for (int i = 0; i < starts.size(); i++) {
                if (TimeOperation.isBetween(time, starts.get(i), ends.get(i), ChronoUnit.SECONDS)) {
                    return key;
                }
            }
        }
        return "error";
    }

    //返回各个计时段持续的时间以及电费单价
    private Map<String, Double[]> getPriceZone(Map<String, Object> powerPriceTime) {
        String[] keys = {"f", "g", "p", "j"};
        Map<String, Double[]> map = new HashMap<>();
        for (String key : keys) {
            Map<String, Object> timeAndPrice = (Map<String, Object>) powerPriceTime.get(key);
            List<LocalTime> starts = (List<LocalTime>) timeAndPrice.get("start");
            List<LocalTime> ends = (List<LocalTime>) timeAndPrice.get("end");
            double price = Double.parseDouble((String) timeAndPrice.get("price"));
            double tmp = 0;
            for (int i = 0; i < starts.size(); i++) {
                double t = ChronoUnit.SECONDS.between(starts.get(i), ends.get(i));
                if (t < 0) t = 86400 + t;
                tmp += t;
            }
            map.put(key, new Double[]{tmp / 3600, price});
        }
        return map;
    }

    //获取一天数据
    private Map<String, Object> getDayData(Integer deviceId, ZonedDateTime zonedDateTime) {
        PowerBillByDay powerBillByDay = powerBillByDayRepository.findByDeviceIdAndDateTime(deviceId, zonedDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        if (powerBillByDay == null) {
            powerBillByDay = insertDay(deviceId, zonedDateTime);
        }
        return powerBillByDay.toMap();
    }


    //获取一个月的数据
    private Map<String, Object> getMonthData(Integer deviceId, Integer year, Integer month) {
        Map<String, Object> dataMap = queryByMonth(deviceId, year, month);
        if (dataMap.get("state") == "Success") {
            return getTotal(dataMap);
        } else {
            throw new NullPointerException(month + "月份数据错误");
        }
    }

    //获取一年的数据
    private Map<String, Object> getYearData(Integer deviceId, Integer year) {
        Map<String, Object> dataMap = queryByYear(deviceId, year);
        if (dataMap.get("state") == "Success") {
            return getTotal(dataMap);
        } else {
            throw new NullPointerException(year + "年数据错误");
        }
    }

    //不同日期同设备的数据累加
    private Map<String, Object> getTotal(Map<String, Object> dataMap) {
        //对数据进行累加
        Map<String, Object> datas = (Map<String, Object>) dataMap.get("data");
        double f_power = 0, g_power = 0, p_power = 0, j_power = 0;
        double f_power_price = 0, g_power_price = 0, p_power_price = 0, j_power_price = 0;
        for (String s : datas.keySet()) {
            Map<String, String> dayData = (Map<String, String>) datas.get(s);
            f_power += Double.parseDouble(dayData.get("f_power"));
            g_power += Double.parseDouble(dayData.get("g_power"));
            p_power += Double.parseDouble(dayData.get("p_power"));
            j_power += Double.parseDouble(dayData.get("j_power"));
            f_power_price += Double.parseDouble(dayData.get("f_power_price"));
            g_power_price += Double.parseDouble(dayData.get("g_power_price"));
            p_power_price += Double.parseDouble(dayData.get("p_power_price"));
            j_power_price += Double.parseDouble(dayData.get("j_power_price"));
        }
        Map<String, Object> tmp = new HashMap<>();
        tmp.put("f_power", String.format("%.2f", f_power));
        tmp.put("f_power_price", String.format("%.2f", f_power_price));
        tmp.put("g_power", String.format("%.2f", g_power));
        tmp.put("g_power_price", String.format("%.2f", g_power_price));
        tmp.put("p_power", String.format("%.2f", p_power));
        tmp.put("p_power_price", String.format("%.2f", p_power_price));
        tmp.put("j_power", String.format("%.2f", j_power));
        tmp.put("j_power_price", String.format("%.2f", j_power_price));

        tmp.put("total_power", String.format("%.2f", f_power + p_power + g_power + j_power));
        tmp.put("total_power_price", String.format("%.2f", f_power_price + p_power_price + g_power_price + j_power_price));
        return tmp;
    }

    //同日期不同设备的数据累加
    private Map<String, Object> sumResult(List<Map<String, Object>> results) {
        Map<String, Object> result = results.get(0);

        for (int i = 1; i < results.size(); i++) {

            Map<String, Object> tmp = results.get(i);
            result.merge("total_power", tmp.get("total_power"),
                    (prev, one) -> String.format("%.2f", Double.parseDouble((String) prev) + Double.parseDouble((String) one)));
            result.merge("total_power_price", tmp.get("total_power_price"),
                    (prev, one) -> String.format("%.2f", Double.parseDouble((String) prev) + Double.parseDouble((String) one)));

            Map<String, Map<String, Object>> transformerBillTotalData = (Map<String, Map<String, Object>>) result.get("data");
            Map<String, Map<String, Object>> tmpData = (Map<String, Map<String, Object>>) tmp.get("data");

            tmpData.forEach((key1, value1) -> {
                Map<String, Object> transformerBillTotalDataValue = transformerBillTotalData.get(key1);
                value1.forEach((key2, value2) -> {
                    transformerBillTotalDataValue.merge(key2, value2,
                            (prev, one) -> String.format("%.2f", Double.parseDouble((String) prev) + Double.parseDouble((String) one)));
                });
            });
        }
        return result;
    }

    /**
     * 根据输入数据，查询PowerRealtime计算出结果录入数据表
     *
     * @param deviceId      设备ID
     * @param zonedDateTime 插入时间 以0点为准
     */
    private PowerBillByDay insertDay(Integer deviceId, ZonedDateTime zonedDateTime) {
        try {
            ZoneId zoneId = ZoneId.from(zonedDateTime);

            //防止传入数据非0点0分
            ZonedDateTime finalZonedDateTime = zonedDateTime.withHour(0).withMinute(0).withSecond(0);
            int time = (int) finalZonedDateTime.toEpochSecond();

            //获得当天电费计价信息
            Map<String, Object> powerPriceTime = powerPriceTimeService.getDevicePowerPriceInTime(time);

            //获得当天的所有电量数据
            List<PowerRealtime> powerRealtimes = powerRealtimeRepository.findAllByDeviceIdAndDataTimeBetween(
                    deviceId, time, time + 86399);


            //初始化表格
            PowerBillByDay powerBillByDay = new PowerBillByDay();
            powerBillByDay.setDeviceId(deviceId);
            powerBillByDay.setDateTime(finalZonedDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

            //利用stream流分割，将各个时间段内的负载求平均值
            Map<String, Double> stringDoubleMap = powerRealtimes.stream().collect(groupingBy(
                    s -> getTimeZone(powerPriceTime, LocalTime.from(TimeOperation.getZonedDateTime(s.getDataTime(), zoneId))),
                    averagingDouble(s -> Double.parseDouble(s.getTotalLoad()))));

            //将负载乘时间和电价，得出电量和电费
            Map<String, Double[]> priceZone = getPriceZone(powerPriceTime);
            String[] keys = {"f", "g", "p", "j"};
            Map<String, Double> power = new HashMap<>();
            Map<String, Double> price = new HashMap<>();
            for (String key : keys) {
                stringDoubleMap.putIfAbsent(key, 0.0);
                power.put(key, stringDoubleMap.get(key) * priceZone.get(key)[0]);
                price.put(key, stringDoubleMap.get(key) * priceZone.get(key)[0] * priceZone.get(key)[1]);
            }


            powerBillByDay.setF_power(String.format("%.2f", power.get("f")));
            powerBillByDay.setG_power(String.format("%.2f", power.get("g")));
            powerBillByDay.setP_power(String.format("%.2f", power.get("p")));
            powerBillByDay.setJ_power(String.format("%.2f", power.get("j")));
            powerBillByDay.setF_power_price(String.format("%.2f", price.get("f")));
            powerBillByDay.setG_power_price(String.format("%.2f", price.get("g")));
            powerBillByDay.setP_power_price(String.format("%.2f", price.get("p")));
            powerBillByDay.setJ_power_price(String.format("%.2f", price.get("j")));

            //如果一天还没过完则不持久化
            if ((System.currentTimeMillis() / 1000 - time) > 86400) {
                powerBillByDayRepository.save(powerBillByDay);
            }
            return powerBillByDay;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    @Override
    public Map<String, Object> queryByMonth(Integer deviceId, Integer year, Integer month) {
        final ZoneId zoneId = utilService.getZoneId();
        Map<String, Object> map = new HashMap<>(); //总容器
        try {
            YearMonth yearMonth = YearMonth.of(year, month);

            //从数据库中查找该月数据
            List<PowerBillByDay> powerBillByDays = powerBillByDayRepository.findAllByDeviceIdAndDateTimeStartingWith(
                    deviceId,
                    yearMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"))
            );


            //天数不够则寻找不存在的天数并插入数据后返回
            if (powerBillByDays.size() < yearMonth.lengthOfMonth()) {
                for (LocalDate localDate = LocalDate.of(year, month, 1); localDate.getMonthValue() == month && localDate.compareTo(LocalDate.now(zoneId)) <= 0; localDate = localDate.plusDays(1)) {
                    LocalDate finalLocalDate = localDate;
                    if (powerBillByDays.stream().noneMatch(p -> Objects.equals(p.getDateTime(), finalLocalDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))))) {
                        PowerBillByDay powerBillByDay = insertDay(deviceId, localDate.atStartOfDay(zoneId));
                        if (powerBillByDay != null) powerBillByDays.add(powerBillByDay);
                    }
                }
            }
            //从对象中提取数据注入Map
            Map<String, Object> dayData = powerBillByDays.stream().filter(Objects::nonNull).collect(Collectors.toMap(
                    PowerBillByDay::getDateTime,
                    PowerBillByDay::toMap,
                    (p1, p2) -> p1, TreeMap::new));

            map.put("data", dayData);

            Map<String, Object> total = getTotal(map);
            map.put("total_power", total.get("total_power"));
            map.put("total_power_price", total.get("total_power_price"));

            map.put("state", "Success");
        } catch (Exception e) {
            map.put("state", "Fail");
            e.printStackTrace();
        }
        return map;
    }

    @Override
    public Map<String, Object> queryByYear(Integer deviceId, Integer year) {
        Map<String, Object> map = new HashMap<>(); //总容器
        try {
            Map<String, Object> data = new TreeMap<>();

            //判断是不是当年，不计算未出现的月份
            YearMonth now = YearMonth.now();
            int end;
            if (year == now.getYear()) {
                end = now.getMonthValue();
            } else {
                end = 12;
            }

            //放入每月的数据
            for (int i = 1; i <= end; i++) {
                data.put(YearMonth.of(year, i).format(DateTimeFormatter.ofPattern("yyyy-MM")), getMonthData(deviceId, year, i));
            }

            map.put("data", data);

            Map<String, Object> total = getTotal(map);
            map.put("total_power", total.get("total_power"));
            map.put("total_power_price", total.get("total_power_price"));

            map.put("state", "Success");
        } catch (Exception e) {
            map.put("state", "Fail");
            e.printStackTrace();
        }
        return map;
    }

    @Override
    public Map<String, Object> dailyUpdate() {
        final ZoneId zoneId = utilService.getZoneId();
        Map<String, Object> map = new HashMap<>();
        try {
            List<Device> devices = deviceRepository.findAll();
            ZonedDateTime zonedDateTime = LocalDate.now().minusDays(1).atStartOfDay(zoneId);

            devices.forEach(device -> {
                PowerBillByDay powerBillByDay = powerBillByDayRepository.findByDeviceIdAndDateTime(device.getId(), zonedDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                if (powerBillByDay == null) {
                    insertDay(device.getId(), zonedDateTime);
                }
            });
            map.put("state", "Success");
        } catch (Exception e) {
            map.put("state", "Fail");
            e.printStackTrace();
        }
        return map;
    }

    @Override
    public Map<String, Object> queryCustom(String deviceIds, String start, String end, String unit) {
        Map<String, Object> map = new TreeMap<>(); //总容器
        final ZoneId zoneId = utilService.getZoneId();


        try {
            //初始化数据
            LocalDate startDate, endDate;
            ChronoUnit chronoUnit;
            String[] devices = deviceIds.split(",");
            switch (unit) {
                case "day":
                    startDate = LocalDate.parse(start, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    endDate = LocalDate.parse(end, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    chronoUnit = ChronoUnit.DAYS;
                    break;
                case "month":
                    startDate = YearMonth.parse(start, DateTimeFormatter.ofPattern("yyyy-MM")).atDay(1);
                    endDate = YearMonth.parse(end, DateTimeFormatter.ofPattern("yyyy-MM")).atDay(1);
                    chronoUnit = ChronoUnit.MONTHS;
                    break;
                case "year":
                    startDate = Year.parse(start, DateTimeFormatter.ofPattern("yyyy")).atDay(1);
                    endDate = Year.parse(end, DateTimeFormatter.ofPattern("yyyy")).atDay(1);
                    chronoUnit = ChronoUnit.YEARS;
                    break;
                default:
                    throw new NullPointerException("指定参数不正确");
            }

            //设备id对应的设备名称
            Map<String, Object> devicesName = Arrays.stream(devices).collect(toMap(d -> d, d -> deviceRepository.findById(Integer.valueOf(d)).get().getName()));
            map.put("devicesName", devicesName);

            //放入数据
            Map<String, Object> data = new TreeMap<>();
            for (LocalDate i = startDate; chronoUnit.between(i, endDate) >= 0; i = i.plus(1, chronoUnit)) {
                String key = null;
                Map<String, Object> deviceData = new TreeMap<>();
                LocalDate finalI = i;
                switch (unit) {
                    case "day":
                        key = i.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                        deviceData = Arrays.stream(devices).collect(toMap(d -> d, d -> getDayData(Integer.valueOf(d), finalI.atStartOfDay(zoneId))));
                        break;
                    case "month":
                        key = i.format(DateTimeFormatter.ofPattern("yyyy-MM"));
                        deviceData = Arrays.stream(devices).collect(toMap(d -> d, d -> getMonthData(Integer.valueOf(d), finalI.getYear(), finalI.getMonthValue())));
                        break;
                    case "year":
                        key = i.format(DateTimeFormatter.ofPattern("yyyy"));
                        deviceData = Arrays.stream(devices).collect(toMap(d -> d, d -> getYearData(Integer.valueOf(d), finalI.getYear())));
                        break;
                }
                data.put(key, deviceData);
            }

            map.put("data", data);


            map.put("state", "Success");
        } catch (Exception e) {
            map.put("state", "Fail");
            e.printStackTrace();
        }

        return map;
    }

    @Override
    public Map<String, Object> queryCostMonth(Integer year, Integer month, String department) {
        Map<String, Object> map = new HashMap<>(); //总容器
        try {
            //获得设备树，得到主变列表
            List<Integer> transformers = utilService.getMainTransformer(utilService.findAllDeviceRelationship(1, department, 0));

            //初始化数据
            List<Map<String, Object>> now = transformers.stream().map(t -> queryByMonth(t, year, month)).collect(toList());
            List<Map<String, Object>> lastMonth = transformers.stream().map(t -> queryByMonth(t, month == 1 ? year - 1 : year, month == 1 ? 12 : month - 1)).collect(toList());
            List<Map<String, Object>> lastYear = transformers.stream().map(t -> queryByMonth(t, year - 1, month)).collect(toList());

            map.put("now", sumResult(now));
            map.put("lastMonth", sumResult(lastMonth));
            map.put("lastYear", sumResult(lastYear));

            map.put("state", "Success");
        } catch (Exception e) {
            map.put("state", "Fail");
            e.printStackTrace();
        }

        return map;
    }

    @Override
    public Map<String, Object> queryCostYear(Integer year, String department) {
        Map<String, Object> map = new HashMap<>(); //总容器
        try {
            //获得设备树，得到主变列表
            List<Integer> transformers = utilService.getMainTransformer(utilService.findAllDeviceRelationship(1, department, 0));

            //初始化数据
            List<Map<String, Object>> now = transformers.stream().map(t -> queryByYear(t, year)).collect(toList());
            List<Map<String, Object>> lastYear = transformers.stream().map(t -> queryByYear(t, year - 1)).collect(toList());

            map.put("now", sumResult(now));
            map.put("lastYear", sumResult(lastYear));

            map.put("state", "Success");
        } catch (Exception e) {
            map.put("state", "Fail");
            e.printStackTrace();
        }

        return map;
    }

    @Override
    public Map<String, Object> monthReport(Integer year, Integer month, String department) {
        Map<String, Object> map = new HashMap<>(); //总容器
        try {
            //获得设备树，得到主变列表
            List deviceTree = utilService.findAllDeviceRelationship(1, department, 0);
            List<Integer> transformers = utilService.getMainTransformer(deviceTree);
            List<Integer> devices = utilService.getAllDevicesId(deviceTree);
            //去除展馆的id
            devices.remove(0);
            //获取月份对应的时间戳
            Integer month_time = (int) LocalDate.of(year, month, 1).atStartOfDay(utilService.getZoneId()).toEpochSecond();

            //加入电费信息
            List<Map<String, Object>> cost = transformers.stream().map(t -> {
                Map<String, Object> data = queryByMonth(t, year, month);
                data.put("deviceId", t);
                return data;
            }).collect(toList());
            List<PowerAnalyseMonthAvg> info = devices.stream().map(id -> powerAnalyseMonthService.getMonthAvgByID(id, month_time)).collect(toList());
            Map<String, Object> devicesName = devices.stream().collect(toMap(String::valueOf, d -> deviceRepository.findById(Integer.valueOf(d)).get().getName()));

            map.put("devicesName", devicesName);
            map.put("cost", cost);
            map.put("info", info);
            map.put("state", "Success");
        } catch (Exception e) {
            map.put("state", "Fail");
            e.printStackTrace();
        }
        return map;
    }
}
