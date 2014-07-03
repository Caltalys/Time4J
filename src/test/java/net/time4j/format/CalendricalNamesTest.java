package net.time4j.format;

import net.time4j.Meridiem;
import net.time4j.Month;
import net.time4j.PlainDate;
import net.time4j.PlainTime;
import net.time4j.Quarter;
import net.time4j.Weekday;
import net.time4j.engine.AttributeQuery;
import net.time4j.engine.Chronology;
import net.time4j.format.CalendarText.Provider;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ServiceLoader;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


@RunWith(JUnit4.class)
public class CalendricalNamesTest {

    @Test
    public void getInstance_Chronology_Locale() {
        Chronology<PlainTime> chronology = Chronology.lookup(PlainTime.class);
        Locale locale = Locale.GERMAN;
        CalendarText expResult =
            CalendarText.getInstance("iso8601", locale);
        CalendarText result =
            CalendarText.getInstance(chronology, locale);
        assertThat(result, is(expResult));
    }

    @Test
    public void getInstance_String_Locale() {
        Locale locale = Locale.US;
        CalendarText result = CalendarText.getInstance("iso8601", locale);
        Provider p = null;
        ClassLoader cl = Thread.currentThread().getContextClassLoader();

        if (cl == null) {
            cl = Provider.class.getClassLoader();
        }

        for (Provider tmp : ServiceLoader.load(Provider.class, cl)) {
            if (
                isCalendarTypeSupported(tmp, "iso8601")
                && isLocaleSupported(tmp, locale)
            ) {
                p = tmp;
                break;
            }
        }

        if (p == null) {
            assertThat(result.toString().contains("JdkProvider"), is(true));
        } else {
            assertThat(result.toString().equals(p.toString()), is(true));
        }

        result = CalendarText.getInstance("xyz", locale);
        assertThat(result.toString().contains("FallbackProvider"), is(true));
    }

    @Test
    public void printMonths() {
        TextWidth textWidth = TextWidth.NARROW;
        CalendarText instance =
           CalendarText.getInstance("iso8601", Locale.GERMAN);
        String result =
            instance.getStdMonths(textWidth, OutputContext.FORMAT)
            .print(Month.MARCH);
        assertThat(result, is("M"));

        textWidth = TextWidth.WIDE;
        result =
            instance.getStdMonths(textWidth, OutputContext.FORMAT)
            .print(Month.MARCH);
        assertThat(result, is("März"));

        textWidth = TextWidth.WIDE;
        result =
            instance.getStdMonths(textWidth, OutputContext.STANDALONE)
            .print(Month.MARCH);
        assertThat(result, is("März"));

        textWidth = TextWidth.SHORT;
        result =
            instance.getStdMonths(textWidth, OutputContext.FORMAT)
            .print(Month.MARCH);
        assertThat(result, is("Mrz"));

        textWidth = TextWidth.ABBREVIATED;
        result =
            instance.getStdMonths(textWidth, OutputContext.FORMAT)
            .print(Month.MARCH);
        assertThat(result, is("Mrz"));
    }

    @Test
    public void parseMonths() {
        CalendarText instance =
           CalendarText.getInstance("iso8601", Locale.GERMAN);
        OutputContext outputContext = OutputContext.FORMAT;
        ParseLog status = new ParseLog();
        Month value =
            instance.getStdMonths(TextWidth.ABBREVIATED, outputContext)
            .parse("Mrz", status, Month.class);
        assertThat(value, is(Month.MARCH));

        status.reset();
        value =
            instance.getStdMonths(TextWidth.WIDE, outputContext)
            .parse("MÄR", status, Month.class, toAttributes(true, true));
        assertThat(value, is(Month.MARCH));

        status.reset();
        value =
            instance.getStdMonths(TextWidth.WIDE, outputContext)
            .parse("MÄRz", status, Month.class, toAttributes(true, false));
        assertThat(value, is(Month.MARCH));

        status.reset();
        value =
            instance.getStdMonths(TextWidth.SHORT, outputContext)
            .parse("MR", status, Month.class, toAttributes(true, true));
        assertThat(value, is(Month.MARCH));

        status.reset();
        value =
            instance.getStdMonths(TextWidth.SHORT, outputContext)
            .parse("Mrz", status, Month.class);
        assertThat(value, is(Month.MARCH));

        status.reset();
        value =
            instance.getStdMonths(TextWidth.NARROW, outputContext)
            .parse("m", status, Month.class, toAttributes(true, true));
        assertThat(value, is(Month.MARCH));

        Locale locale = Locale.JAPAN;
        DateFormatSymbols dfs = DateFormatSymbols.getInstance(locale);
        instance =
           CalendarText.getInstance("iso8601", locale);
        status.setPosition(0);
        value =
            instance.getStdMonths(TextWidth.NARROW, outputContext)
            .parse(
                dfs.getShortMonths()[Calendar.MARCH],
                status,
                Month.class);
        assertThat(value, is(Month.MARCH));
    }

    @Test
    public void printQuarters() {
        TextWidth textWidth = TextWidth.NARROW;
        OutputContext outputContext = OutputContext.FORMAT;
        CalendarText instance =
           CalendarText.getInstance("iso8601", Locale.GERMAN);
        String result =
            instance.getQuarters(textWidth, outputContext)
            .print(Quarter.Q3);
        assertThat(result, is("3"));

        textWidth = TextWidth.SHORT;
        result =
            instance.getQuarters(textWidth, outputContext)
            .print(Quarter.Q3);
        assertThat(result, is("Q3"));
    }

    @Test
    public void parseQuarters() {
        TextWidth textWidth = TextWidth.SHORT;
        OutputContext outputContext = OutputContext.FORMAT;
        CalendarText instance =
           CalendarText.getInstance("iso8601", Locale.GERMAN);
        Quarter result =
            instance.getQuarters(textWidth, outputContext)
            .parse("Q3", new ParseLog(), Quarter.class);
        assertThat(result, is(Quarter.Q3));
    }

    @Test
    public void printWeekdays() {
        OutputContext outputContext = OutputContext.FORMAT;

        // deutsch

        CalendarText instance =
           CalendarText.getInstance("iso8601", Locale.GERMAN);
        TextWidth textWidth = TextWidth.NARROW;
        String result =
            instance.getWeekdays(textWidth, outputContext)
            .print(Weekday.FRIDAY);
        assertThat(result, is("F"));

        textWidth = TextWidth.SHORT;
        result =
            instance.getWeekdays(textWidth, outputContext)
            .print(Weekday.FRIDAY);
        assertThat(result, is("Fr"));

        textWidth = TextWidth.ABBREVIATED;
        result =
            instance.getWeekdays(textWidth, outputContext)
            .print(Weekday.FRIDAY);
        assertThat(result, is("Fr"));

        textWidth = TextWidth.WIDE;
        result =
            instance.getWeekdays(textWidth, outputContext)
            .print(Weekday.FRIDAY);
        assertThat(result, is("Freitag"));

        // englisch

        instance = CalendarText.getInstance("iso8601", Locale.ENGLISH);

        textWidth = TextWidth.NARROW;
        result =
            instance.getWeekdays(textWidth, outputContext)
            .print(Weekday.FRIDAY);
        assertThat(result, is("F"));

        textWidth = TextWidth.SHORT;
        result =
            instance.getWeekdays(textWidth, outputContext)
            .print(Weekday.FRIDAY);
        assertThat(result, is("Fr"));

        textWidth = TextWidth.ABBREVIATED;
        result =
            instance.getWeekdays(textWidth, outputContext)
            .print(Weekday.FRIDAY);
        assertThat(result, is("Fri"));

        textWidth = TextWidth.WIDE;
        result =
            instance.getWeekdays(textWidth, outputContext)
            .print(Weekday.FRIDAY);
        assertThat(result, is("Friday"));
    }

    @Test
    public void parseWeekdays() {
        CalendarText instance =
           CalendarText.getInstance("iso8601", Locale.GERMAN);
        ParseLog status = new ParseLog();
        Weekday w =
            instance.getWeekdays(TextWidth.WIDE, OutputContext.FORMAT)
            .parse("FRE", status, Weekday.class, toAttributes(true, true));
        assertThat(w, is(Weekday.FRIDAY));

        instance = CalendarText.getInstance("iso8601", Locale.ENGLISH);
        ParseLog status2 = new ParseLog();
        Weekday w2 =
            instance.getWeekdays(TextWidth.WIDE, OutputContext.FORMAT)
            .parse("FRI", status2, Weekday.class, toAttributes(true, true));
        assertThat(w2, is(Weekday.FRIDAY));
    }

    @Test
    public void eras() throws ClassNotFoundException {
        assertThat(
           Chronology.lookup(PlainDate.class)
                .getCalendarSystem()
                .getEras()
                .isEmpty(),
           is(true));
        TextWidth textWidth = TextWidth.WIDE;
        CalendarText instance =
           CalendarText.getInstance("iso8601", Locale.GERMAN);
        Enum<?> e =
            Enum.class.cast(
                Class.forName("net.time4j.SimpleEra").getEnumConstants()[1]);
        String result =
            instance.getEras(textWidth).print(e);
        assertThat(result, is("n. Chr."));
    }

    @Test
    public void printMeridiems() {
        TextWidth textWidth = TextWidth.WIDE;
        CalendarText instance =
           CalendarText.getInstance("iso8601", Locale.ENGLISH);
        String result =
            instance.getMeridiems(textWidth).print(Meridiem.PM);
        assertThat(result, is("PM"));
    }

    @Test
    public void parseMeridiems() {
        TextWidth textWidth = TextWidth.WIDE;
        CalendarText instance =
           CalendarText.getInstance("iso8601", Locale.ENGLISH);
        Meridiem result =
            instance.getMeridiems(textWidth).parse(
                "PM", new ParseLog(), Meridiem.class);
        assertThat(result, is(Meridiem.PM));
    }

    @Test
    public void textFormsDelegate() {
        CalendarText instance =
           CalendarText.getInstance("iso8601", Locale.ENGLISH);
        assertThat(
            instance.getTextForms(PlainTime.AM_PM_OF_DAY).print(Meridiem.PM),
            is("PM"));
    }

    @Test(expected=MissingResourceException.class)
    public void textFormsException() {
        CalendarText instance =
           CalendarText.getInstance("xyz", Locale.ENGLISH);
        instance.getTextForms(PlainTime.AM_PM_OF_DAY);
    }

    private static boolean isCalendarTypeSupported(
        Provider p,
        String calendarType
    ) {

        for (String c : p.getSupportedCalendarTypes()) {
            if (c.equals(calendarType)) {
                return true;
            }
        }

        return false;

    }

    private static boolean isLocaleSupported(
        Provider p,
        Locale locale
    ) {

        for (Locale l : p.getAvailableLocales()) {
            String lang = locale.getLanguage();
            String country = locale.getCountry();

            if (
                lang.equals(l.getLanguage())
                && (country.isEmpty() || country.equals(l.getCountry()))
            ) {
                return true;
            }
        }

        return false;

    }

    private static AttributeQuery toAttributes(
        boolean caseInsensitive,
        boolean partialCompare
    ) {

        Attributes attrs =
            new Attributes.Builder()
            .set(Attributes.PARSE_CASE_INSENSITIVE, caseInsensitive)
            .set(Attributes.PARSE_PARTIAL_COMPARE, partialCompare)
            .build();
        return attrs;

    }

}