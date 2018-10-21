/*
 * -----------------------------------------------------------------------
 * Copyright © 2013-2018 Meno Hochschild, <http://www.menodata.de/>
 * -----------------------------------------------------------------------
 * This file (DefaultLeapSecondProviderSPI.java) is part of project Time4J.
 *
 * Time4J is free software: You can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * Time4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Time4J. If not, see <http://www.gnu.org/licenses/>.
 * -----------------------------------------------------------------------
 */

package net.time4j.scale.spi;

import net.time4j.PlainDate;
import net.time4j.base.GregorianDate;
import net.time4j.base.ResourceLoader;
import net.time4j.format.TemporalFormatter;
import net.time4j.format.platform.SimpleFormatter;
import net.time4j.scale.LeapSecondProvider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import static net.time4j.scale.LeapSeconds.PATH_TO_LEAPSECONDS;


/**
 * <p>{@code ServiceProvider}-implementation for accessing the file
 * &quot;leapseconds.data&quot; in class path. </p>
 *
 * @author  Meno Hochschild
 */
public final class DefaultLeapSecondProviderSPI
    implements LeapSecondProvider {

    //~ Instanzvariablen --------------------------------------------------

    private final String source;
    private final PlainDate expires;
    private final Map<GregorianDate, Integer> table;

    //~ Konstruktoren -----------------------------------------------------

    public DefaultLeapSecondProviderSPI() {
        super();

        PlainDate tmpExpires = PlainDate.axis().getMinimum();
        this.table = new LinkedHashMap<GregorianDate, Integer>(50);
        String name = PATH_TO_LEAPSECONDS;
        URI uri = ResourceLoader.getInstance().locate("core", LeapSecondProvider.class, name);
        InputStream is = ResourceLoader.getInstance().load(uri, true);

        if (is == null) {
            is = LeapSecondProvider.class.getClassLoader().getResourceAsStream(name); // test classes in IDE
        }

        if (is != null) {

            this.source = uri.toString();
            TemporalFormatter<PlainDate> f = SimpleFormatter.ofDatePattern("yyyy-MM-dd", Locale.ROOT);

            try {

                BufferedReader br =
                    new BufferedReader(
                        new InputStreamReader(is, "US-ASCII"));

                String line;

                while ((line = br.readLine()) != null) {

                    if (line.startsWith("#")) {
                        continue; // Kommentarzeile überspringen
                    } else if (line.startsWith("@expires=")) {
                        String date = line.substring(9);
                        tmpExpires = f.parse(date);
                        continue;
                    }

                    int comma = line.indexOf(',');
                    String date;
                    Boolean sign = null;

                    if (comma == -1) {
                        date = line.trim();
                        sign = Boolean.TRUE;
                    } else {
                        date = line.substring(0, comma).trim();
                        String s = line.substring(comma + 1).trim();

                        if (s.length() == 1) {
                            char c = s.charAt(0);
                            if (c == '+') {
                                sign = Boolean.TRUE;
                            } else if (c == '-') {
                                sign = Boolean.FALSE;
                            }
                        }

                        if (sign == null) {
                            throw new IllegalStateException(
                                "Missing leap second sign.");
                        }
                    }

                    int year = Integer.parseInt(date.substring(0, 4));
                    int month = Integer.parseInt(date.substring(5, 7));
                    int dom = Integer.parseInt(date.substring(8, 10));

                    Object old =
                        this.table.put(
                            PlainDate.of(year, month, dom),
                            Integer.valueOf(sign.booleanValue() ? 1 : -1)
                        );

                    if (old != null) {
                        throw new IllegalStateException(
                            "Duplicate leap second event found.");
                    }

                }

            } catch (UnsupportedEncodingException uee) {
                throw new AssertionError(uee);
            } catch (IllegalStateException ise) {
                throw ise;
            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            } finally {
                try {
                    is.close();
                } catch (IOException ioe) {
                    ioe.printStackTrace(System.err);
                }
            }

        } else {
            this.source = "";
            System.err.println("Warning: File \"" + name + "\" not found.");
        }

        this.expires = tmpExpires;

    }

    //~ Methoden ----------------------------------------------------------

    @Override
    public Map<GregorianDate, Integer> getLeapSecondTable() {

        return Collections.unmodifiableMap(this.table);

    }

    @Override
    public boolean supportsNegativeLS() {

        return true;

    }

    @Override
    public GregorianDate getDateOfEvent(
        int year,
        int month,
        int dayOfMonth
    ) {

        return PlainDate.of(year, month, dayOfMonth);

    }

    @Override
    public GregorianDate getDateOfExpiration() {

        return this.expires;

    }

    @Override
    public String toString() {

        return this.source;

    }

}
