package com.jadde.alarmemedicamentos;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class ScheduleFactory {
    private static final String[] PHASE_ONE_BLACK = {
            "07:00", "09:00", "11:00", "13:00", "15:00", "17:00", "19:00", "21:00", "23:00"
    };
    private static final String[] PHASE_TWO_BLACK = {
            "07:00", "10:30", "13:30", "16:30", "19:30", "22:00"
    };
    private static final String[] GREEN = {"08:00", "14:00", "18:30", "22:30"};
    private static final String[] PURPLE = {"10:00", "16:00", "20:00"};

    private ScheduleFactory() {
    }

    public static List<Dose> create(
            int startYear,
            int startMonthZeroBased,
            int startDay,
            String firstDayFrom,
            String bedtime
    ) {
        List<Dose> result = new ArrayList<>();
        int id = 20_000;

        for (int dayIndex = 0; dayIndex < 15; dayIndex++) {
            int phase = dayIndex < 5 ? 1 : 2;
            String[] blackTimes = phase == 1 ? PHASE_ONE_BLACK : PHASE_TWO_BLACK;

            id = addMedicine(
                    result, id, startYear, startMonthZeroBased, startDay, dayIndex,
                    blackTimes, "Vigadexa ou Facoba", Dose.BLACK, phase, firstDayFrom
            );
            id = addMedicine(
                    result, id, startYear, startMonthZeroBased, startDay, dayIndex,
                    GREEN, "Acular", Dose.GREEN, phase, firstDayFrom
            );
            id = addMedicine(
                    result, id, startYear, startMonthZeroBased, startDay, dayIndex,
                    PURPLE, "Azopt ou Dorzal 2,0%", Dose.PURPLE, phase, firstDayFrom
            );

            if (dayIndex > 0 || bedtime.compareTo(firstDayFrom) >= 0) {
                result.add(new Dose(
                        id++,
                        timestamp(startYear, startMonthZeroBased, startDay, dayIndex, bedtime),
                        bedtime,
                        "Cylocort ou Tobracort",
                        Dose.OINTMENT,
                        phase
                ));
            }
        }

        Collections.sort(result, Comparator.comparingLong(dose -> dose.triggerAtMillis));
        return result;
    }

    private static int addMedicine(
            List<Dose> result,
            int id,
            int year,
            int month,
            int day,
            int dayIndex,
            String[] times,
            String medicine,
            String color,
            int phase,
            String firstDayFrom
    ) {
        for (String time : times) {
            if (dayIndex == 0 && time.compareTo(firstDayFrom) < 0) {
                continue;
            }
            result.add(new Dose(
                    id++,
                    timestamp(year, month, day, dayIndex, time),
                    time,
                    medicine,
                    color,
                    phase
            ));
        }
        return id;
    }

    private static long timestamp(
            int year,
            int month,
            int day,
            int dayOffset,
            String time
    ) {
        String[] parts = time.split(":");
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(
                year,
                month,
                day,
                Integer.parseInt(parts[0]),
                Integer.parseInt(parts[1]),
                0
        );
        calendar.add(Calendar.DAY_OF_MONTH, dayOffset);
        return calendar.getTimeInMillis();
    }

    public static int colorValue(String color) {
        switch (color) {
            case Dose.BLACK:
                return 0xFF171918;
            case Dose.GREEN:
                return 0xFF34A853;
            case Dose.PURPLE:
                return 0xFF8250DF;
            case Dose.OINTMENT:
                return 0xFFC27B2A;
            default:
                return 0xFF1F6F54;
        }
    }

    public static String colorLabel(String color) {
        switch (color) {
            case Dose.BLACK:
                return "PRETO";
            case Dose.GREEN:
                return "VERDE";
            case Dose.PURPLE:
                return "ROXO";
            case Dose.OINTMENT:
                return "POMADA";
            default:
                return "REMÉDIO";
        }
    }
}
