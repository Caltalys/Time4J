package net.time4j.range;

import net.time4j.CalendarUnit;
import net.time4j.PlainDate;
import net.time4j.SystemClock;
import net.time4j.Weekday;
import net.time4j.ZonalClock;
import net.time4j.base.GregorianDate;
import net.time4j.base.GregorianMath;
import net.time4j.format.expert.ChronoFormatter;
import net.time4j.format.expert.PatternType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


@RunWith(JUnit4.class)
public class CalendarWeekTest {

    @Test
    public void min() {
        CalendarWeek cw = CalendarWeek.of(GregorianMath.MIN_YEAR, 1);
        assertThat(cw.getYear(), is(GregorianMath.MIN_YEAR));
        assertThat(cw.getWeek(), is(1));
        assertThat(cw.at(Weekday.MONDAY), is(PlainDate.axis().getMinimum()));
    }

    @Test
    public void max1() {
        CalendarWeek cw = CalendarWeek.of(GregorianMath.MAX_YEAR, 52);
        assertThat(cw.getYear(), is(GregorianMath.MAX_YEAR));
        assertThat(cw.getWeek(), is(52));
        assertThat(cw.getEnd().getTemporal(), is(PlainDate.axis().getMaximum()));
        assertThat(cw.at(Weekday.FRIDAY), is(PlainDate.axis().getMaximum()));
    }

    @Test(expected=IllegalArgumentException.class)
    public void max2() {
        CalendarWeek.of(GregorianMath.MAX_YEAR, 52).at(Weekday.SATURDAY);
    }

    @Test(expected=IllegalArgumentException.class)
    public void invalidWeek() {
        CalendarWeek.of(2014, 53);
    }

    @Test
    public void fromGregorianDate() {
        assertThat(
            CalendarWeek.from(PlainDate.of(2016, 2, 29)),
            is(CalendarWeek.of(2016, 9)));
    }

    @Test(expected=IllegalArgumentException.class)
    public void fromGregorianDateInvalid() {
        CalendarWeek.from(
            new GregorianDate() {
                @Override
                public int getYear() {
                    return 2015;
                }

                @Override
                public int getMonth() {
                    return 2;
                }

                @Override
                public int getDayOfMonth() {
                    return 29;
                }
            }
        );
    }

    @Test
    public void getYear() {
        assertThat(CalendarWeek.of(2011, 4).getYear(), is(2011));
    }

    @Test
    public void getWeek() {
        assertThat(CalendarWeek.of(2011, 4).getWeek(), is(4));
    }

    @Test
    public void getStart() {
        assertThat(CalendarWeek.of(2011, 4).getStart(), is(Boundary.ofClosed(PlainDate.of(2011, 1, 24))));
    }

    @Test
    public void getEnd() {
        assertThat(CalendarWeek.of(2011, 4).getEnd(), is(Boundary.ofClosed(PlainDate.of(2011, 1, 30))));
    }

    @Test
    public void compareTo() {
        assertThat(CalendarWeek.of(2012, 3).compareTo(CalendarWeek.of(2012, 4)) < 0, is(true));
        assertThat(CalendarWeek.of(2012, 3).compareTo(CalendarWeek.of(2012, 2)) > 0, is(true));
        assertThat(CalendarWeek.of(2012, 3).compareTo(CalendarWeek.of(2012, 3)) == 0, is(true));
    }

    @Test
    public void atDayOfWeek() {
        CalendarWeek cw = CalendarWeek.of(2012, 3);
        assertThat(
            cw.at(Weekday.MONDAY) == cw.getStart().getTemporal(),
            is(true));
        assertThat(
            cw.at(Weekday.FRIDAY),
            is(PlainDate.of(2012, 1, 20)));
    }

    @Test
    public void currentValue() {
        ZonalClock clock = SystemClock.inLocalView();
        CalendarWeek cw = clock.now(CalendarWeek.chronology());
        assertThat(cw.getYear(), is(clock.today().get(PlainDate.YEAR_OF_WEEKDATE)));
        assertThat(cw.getWeek(), is(clock.today().get(CalendarWeek.WEEK_OF_YEAR)));
    }

    @Test
    public void iterator() {
        int count = 1;
        for (PlainDate date : CalendarWeek.of(2016, 4)) {
            System.out.println(count + " => " + date);
            count++;
        }
        assertThat(count - 1, is(7));
    }

    @Test
    public void length() {
        assertThat(CalendarWeek.of(2016, 3).length(), is(7));
    }

    @Test
    public void contains() {
        assertThat(CalendarWeek.of(2016, 1).contains(CalendarWeek.YEAR_OF_WEEKDATE), is(true));
        assertThat(CalendarWeek.of(2016, 1).contains(CalendarWeek.WEEK_OF_YEAR), is(true));

        assertThat(CalendarWeek.of(2016, 24).contains(PlainDate.of(2016, 6, 12)), is(false));
        assertThat(CalendarWeek.of(2016, 24).contains(PlainDate.of(2016, 6, 13)), is(true));
        assertThat(CalendarWeek.of(2016, 24).contains(PlainDate.of(2016, 6, 14)), is(true));
        assertThat(CalendarWeek.of(2016, 24).contains(PlainDate.of(2016, 6, 19)), is(true));
        assertThat(CalendarWeek.of(2016, 24).contains(PlainDate.of(2016, 6, 20)), is(false));
    }

    @Test
    public void isAfter() {
        assertThat(CalendarWeek.of(2016, 24).isAfter(PlainDate.of(2016, 6, 13)), is(false));
        assertThat(CalendarWeek.of(2016, 24).isAfter(PlainDate.of(2016, 6, 12)), is(true));

        assertThat(CalendarWeek.of(2016, 24).isAfter(CalendarWeek.of(2016, 23)), is(true));
        assertThat(CalendarWeek.of(2016, 24).isAfter(CalendarWeek.of(2016, 24)), is(false));
    }

    @Test
    public void isBefore() {
        assertThat(CalendarWeek.of(2016, 24).isBefore(PlainDate.of(2016, 6, 20)), is(true));
        assertThat(CalendarWeek.of(2016, 24).isBefore(PlainDate.of(2016, 6, 19)), is(false));

        assertThat(CalendarWeek.of(2016, 24).isBefore(CalendarWeek.of(2016, 25)), is(true));
        assertThat(CalendarWeek.of(2016, 24).isBefore(CalendarWeek.of(2016, 24)), is(false));
    }

    @Test
    public void isSimultaneous() {
        assertThat(CalendarWeek.of(2016, 24).isSimultaneous(CalendarWeek.of(2016, 25)), is(false));
        assertThat(CalendarWeek.of(2016, 24).isSimultaneous(CalendarWeek.of(2016, 24)), is(true));
    }

    @Test
    public void plusYears() {
        assertThat(
            CalendarWeek.of(2012, 24).plus(Years.ofWeekBased(4)),
            is(CalendarWeek.of(2016, 24)));
        assertThat(
            CalendarWeek.of(2015, 53).plus(Years.ofWeekBased(1)),
            is(CalendarWeek.of(2016, 52)));
    }

    @Test
    public void minusYears() {
        assertThat(
            CalendarWeek.of(2012, 24).minus(Years.ofWeekBased(4)),
            is(CalendarWeek.of(2008, 24)));
        assertThat(
            CalendarWeek.of(2015, 53).minus(Years.ofWeekBased(1)),
            is(CalendarWeek.of(2014, 52)));
    }

    @Test
    public void plusWeeks() {
        assertThat(
            CalendarWeek.of(2012, 24).plus(Weeks.of(7)),
            is(CalendarWeek.of(2012, 31)));
    }

    @Test
    public void minusWeeks() {
        assertThat(
            CalendarWeek.of(2016, 1).minus(Weeks.of(7)),
            is(CalendarWeek.of(2015, 47)));
    }

    @Test
    public void formatISO() {
        CalendarWeek cw = CalendarWeek.of(2012, 3);
        assertThat(
            cw.toString(),
            is("2012-W03"));
    }

    @Test
    public void parseISO() throws ParseException {
        CalendarWeek expected = CalendarWeek.of(2012, 3);
        assertThat(
            CalendarWeek.parseISO("2012-W03"),
            is(expected));
        assertThat(
            CalendarWeek.parseISO("2012W03"),
            is(expected));
    }

    @Test
    public void formatKW() {
        ChronoFormatter<CalendarWeek> f =
            ChronoFormatter.setUp(CalendarWeek.chronology(), Locale.ROOT)
                .addPattern("w. 'KW'", PatternType.CLDR).build();
        assertThat(
            f.format(CalendarWeek.of(2016, 4)),
            is("4. KW"));
    }

    @Test
    public void withLastWeekOfYear() {
        assertThat(
            CalendarWeek.of(2015, 1).withLastWeekOfYear(),
            is(CalendarWeek.of(2015, 53)));
    }

    @Test
    public void isValid() {
        assertThat(
            CalendarWeek.isValid(2015, 1),
            is(true));
        assertThat(
            CalendarWeek.isValid(2015, 53),
            is(true));
        assertThat(
            CalendarWeek.isValid(2015, 0),
            is(false));
        assertThat(
            CalendarWeek.isValid(2015, 54),
            is(false));
        assertThat(
            CalendarWeek.isValid(2016, 52),
            is(true));
        assertThat(
            CalendarWeek.isValid(2016, 53),
            is(false));
    }

    @Test
    public void nowInSystemTime() {
        assertThat(
            CalendarWeek.nowInSystemTime(),
            is(SystemClock.inLocalView().now(CalendarWeek.chronology())));
    }

    @Test
    public void streamDaily() {
        List<PlainDate> expected = new ArrayList<>();
        expected.add(PlainDate.of(2016, 1, 25));
        expected.add(PlainDate.of(2016, 1, 26));
        expected.add(PlainDate.of(2016, 1, 27));
        expected.add(PlainDate.of(2016, 1, 28));
        expected.add(PlainDate.of(2016, 1, 29));
        expected.add(PlainDate.of(2016, 1, 30));
        expected.add(PlainDate.of(2016, 1, 31));
        assertThat(CalendarWeek.of(2016, 4).streamDaily().collect(Collectors.toList()), is(expected));
    }

}