package co.aegisrpg.api.common.utils;

import co.aegisrpg.api.common.annotations.Disabled;
import co.aegisrpg.api.common.annotations.Environments;
import io.github.classgraph.AnnotationEnumValue;
import io.github.classgraph.AnnotationInfo;
import io.github.classgraph.AnnotationParameterValue;
import io.github.classgraph.ClassInfo;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Utils {
    public static final String ALPHANUMERICS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    public Utils() {
    }

    public static <K extends Comparable<? super K>, V> LinkedHashMap<K, V> sortByKey(Map<K, V> map) {
        return collect(map.entrySet().stream().sorted(Entry.comparingByKey()));
    }

    public static <K, V extends Comparable<? super V>> LinkedHashMap<K, V> sortByValue(Map<K, V> map) {
        return collect(map.entrySet().stream().sorted(Entry.comparingByValue()));
    }

    public static <K extends Comparable<? super K>, V> LinkedHashMap<K, V> sortByKeyReverse(Map<K, V> map) {
        return reverse(sortByKey(map));
    }

    public static <K, V extends Comparable<? super V>> LinkedHashMap<K, V> sortByValueReverse(Map<K, V> map) {
        return reverse(sortByValue(map));
    }

    public static <K, V> LinkedHashMap<K, V> collect(Stream<Map.Entry<K, V>> stream) {
        return (LinkedHashMap)stream.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> {
            return e1;
        }, LinkedHashMap::new));
    }

    public static <K, V> LinkedHashMap<K, V> reverse(LinkedHashMap<K, V> sorted) {
        LinkedHashMap<K, V> reverse = new LinkedHashMap();
        List<K> keys = new ArrayList(sorted.keySet());
        Collections.reverse(keys);
        keys.forEach((key) -> {
            reverse.put(key, sorted.get(key));
        });
        return reverse;
    }

    public static <T> List<T> reverse(List<T> list) {
        Collections.reverse(list);
        return list;
    }

    public static <T> MinMaxResult<T> getMax(Collection<T> things, Function<T, Number> getter) {
        return getMinMax(things, getter, Utils.ComparisonOperator.GREATER_THAN);
    }

    public static <T> MinMaxResult<T> getMin(Collection<T> things, Function<T, Number> getter) {
        return getMinMax(things, getter, Utils.ComparisonOperator.LESS_THAN);
    }

    private static <T> MinMaxResult<T> getMinMax(Collection<T> things, Function<T, Number> getter, ComparisonOperator operator) {
        Number number = operator == Utils.ComparisonOperator.LESS_THAN ? Double.MAX_VALUE : 0.0;
        T result = null;
        Iterator var5 = things.iterator();

        while(var5.hasNext()) {
            T thing = var5.next();
            Number value = (Number)getter.apply(thing);
            if (value != null && operator.run(value.doubleValue(), ((Number)number).doubleValue())) {
                number = value;
                result = thing;
            }
        }

        return new MinMaxResult(result, (Number)number);
    }

    public static Map<String, String> dump(final Object object) {
        Map<String, String> output = new HashMap();
        List<Method> methods = new ArrayList<Method>() {
            {
                Iterator var2 = ReflectionUtils.superclassesOf(object.getClass()).iterator();

                while(var2.hasNext()) {
                    Class<?> superclass = (Class)var2.next();
                    this.addAll(Arrays.asList(superclass.getDeclaredMethods()));
                }

            }
        };
        Iterator var3 = methods.iterator();

        while(var3.hasNext()) {
            Method method = (Method)var3.next();
            if (method.getName().matches("^(get|is|has).*") && method.getParameterCount() == 0) {
                try {
                    Object invoke = method.invoke(object);
                    output.put(method.getName(), invoke == null ? null : invoke.toString());
                } catch (InvocationTargetException | IllegalAccessException var6) {
                    var6.printStackTrace();
                }
            }
        }

        return output;
    }

    public static LocalDateTime epochSecond(String timestamp) {
        try {
            return epochSecond(Long.parseLong(timestamp));
        } catch (NumberFormatException var3) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss xxxx");
            return LocalDateTime.parse(timestamp, formatter);
        }
    }

    public static LocalDateTime epochSecond(long timestamp) {
        return epochMilli(String.valueOf(timestamp).length() == 13 ? timestamp : timestamp * 1000L);
    }

    public static LocalDateTime epochMilli(long timestamp) {
        return Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    public static boolean isBetween(LocalDateTime dateTime, LocalDateTime start, LocalDateTime end) {
        return !dateTime.isBefore(start) && !dateTime.isAfter(end);
    }

    public static boolean isBetween(LocalDate dateTime, LocalDate start, LocalDate end) {
        return !dateTime.isBefore(start) && !dateTime.isAfter(end);
    }

    public static boolean canEnable(Class<?> clazz) {
        if (clazz.getSimpleName().startsWith("_")) {
            return false;
        } else if (Modifier.isAbstract(clazz.getModifiers())) {
            return false;
        } else if (Modifier.isInterface(clazz.getModifiers())) {
            return false;
        } else if (clazz.getAnnotation(Disabled.class) != null) {
            return false;
        } else {
            return clazz.getAnnotation(Environments.class) == null || Env.applies(((Environments)clazz.getAnnotation(Environments.class)).value());
        }
    }

    public static boolean canEnable(ClassInfo clazz) {
        if (clazz.getSimpleName().startsWith("_")) {
            return false;
        } else if (Modifier.isAbstract(clazz.getModifiers())) {
            return false;
        } else if (Modifier.isInterface(clazz.getModifiers())) {
            return false;
        } else if (clazz.getAnnotationInfo(Disabled.class) != null) {
            return false;
        } else {
            AnnotationInfo environments = clazz.getAnnotationInfo(Environments.class);
            if (environments != null) {
                List<Env> envs = Arrays.stream((Object[])((AnnotationParameterValue)environments.getParameterValues().get("value")).getValue()).map((obj) -> {
                    return (AnnotationEnumValue)obj;
                }).map((value) -> {
                    return Env.valueOf(value.getValueName());
                }).toList();
                if (!Env.applies(envs)) {
                    return false;
                }
            }

            return true;
        }
    }

    public static <T> List<T> combine(final Collection<T>... lists) {
        return new ArrayList<T>() {
            {
                Collection[] var2 = lists;
                int var3 = var2.length;

                for(int var4 = 0; var4 < var3; ++var4) {
                    Collection<T> list = var2[var4];
                    this.addAll(list);
                }

            }
        };
    }

    public static int getFirstIndexOf(Collection<?> collection, Object object) {
        Iterator<?> iterator = collection.iterator();
        int index = 0;

        while(iterator.hasNext()) {
            Object next = iterator.next();
            if (next == null) {
                if (object == null) {
                    return index;
                }

                ++index;
            } else {
                if (next.equals(object)) {
                    return index;
                }

                ++index;
            }
        }

        return -1;
    }

    public static boolean attempt(int times, BooleanSupplier to) {
        int count = 0;

        do {
            ++count;
            if (count > times) {
                return false;
            }
        } while(!to.getAsBoolean());

        return true;
    }

    public static boolean isLong(String text) {
        try {
            Long.parseLong(text);
            return true;
        } catch (Exception var2) {
            return false;
        }
    }

    public static boolean isInt(String text) {
        try {
            Integer.parseInt(text);
            return true;
        } catch (Exception var2) {
            return false;
        }
    }

    public static boolean isDouble(String text) {
        try {
            Double.parseDouble(text);
            return true;
        } catch (Exception var2) {
            return false;
        }
    }

    public static String createSha1(String url) {
        try {
            OkHttpClient client = new OkHttpClient();
            Request request = (new Request.Builder()).url(url).build();
            Response response = client.newCall(request).execute();
            return createSha1(response.body());
        } catch (Throwable var4) {
            throw var4;
        }
    }
    `
    public static String createSha1(ResponseBody body) {
        try {
            return body != null ? SHAsum(body.bytes()) : null;
        } catch (Throwable var2) {
            throw var2;
        }
    }

    public static String SHAsum(byte[] bytes) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        return byteArray2Hex(md.digest(bytes));
    }

    private static String byteArray2Hex(final byte[] hash) {
        Formatter formatter = new Formatter();
        byte[] var2 = hash;
        int var3 = hash.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            byte b = var2[var4];
            formatter.format("%02x", b);
        }

        return formatter.toString();
    }

    public static <T> T getDefaultPrimitiveValue(Class<T> clazz) {
        return Array.get(Array.newInstance(clazz, 1), 0);
    }

    public static boolean isBoolean(Parameter parameter) {
        return parameter.getType() == Boolean.class || parameter.getType() == Boolean.TYPE;
    }

    @Contract(
            mutates = "param2"
    )
    public static <T> T removeFirstIf(Predicate<T> predicate, Iterable<T> from) {
        Objects.requireNonNull(predicate, "predicate");
        Objects.requireNonNull(from, "from");
        Iterator<T> iterator = from.iterator();

        Object item;
        do {
            if (!iterator.hasNext()) {
                return null;
            }

            item = iterator.next();
        } while(!predicate.test(item));

        iterator.remove();
        return item;
    }

    @Contract(
            mutates = "param2"
    )
    public static <T> boolean removeIf(Predicate<T> predicate, Iterable<T> from) {
        Objects.requireNonNull(predicate, "predicate");
        Objects.requireNonNull(from, "from");
        boolean removed = false;
        Iterator<T> iterator = from.iterator();

        while(iterator.hasNext()) {
            T item = iterator.next();
            if (predicate.test(item)) {
                iterator.remove();
                removed = true;
            }
        }

        return removed;
    }

    @Contract(
            mutates = "param2"
    )
    public static <T> boolean removeIf(Predicate<T> predicate, Consumer<T> consumer, Iterable<T> from) {
        Objects.requireNonNull(predicate, "predicate");
        Objects.requireNonNull(from, "from");
        boolean removed = false;
        Iterator<T> iterator = from.iterator();

        while(iterator.hasNext()) {
            T item = iterator.next();
            if (predicate.test(item)) {
                consumer.accept(item);
                iterator.remove();
                removed = true;
            }
        }

        return removed;
    }

    public static <T> boolean removeAll(T item, Iterable<T> from) {
        return removeIf((object) -> {
            return Objects.equals(object, item);
        }, from);
    }

    public static <T> boolean removeAll(T item, Consumer<T> consumer, Iterable<T> from) {
        return removeIf((object) -> {
            return Objects.equals(object, item);
        }, consumer, from);
    }

    public static <T> @NotNull List<T> mutableCopyOf(@Nullable List<T> list) {
        return new ArrayList(list == null ? Collections.emptyList() : list);
    }

    public static String bash(String command) {
        try {
            return bash(command, (File)null);
        } catch (Throwable var2) {
            throw var2;
        }
    }

    public static String bash(String command, File directory) {
        try {
            InputStream result = Runtime.getRuntime().exec(command, (String[])null, directory).getInputStream();
            StringBuilder builder = new StringBuilder();
            (new Scanner(result)).forEachRemaining((string) -> {
                builder.append(string).append(" ");
            });
            return builder.toString().trim();
        } catch (Throwable var4) {
            throw var4;
        }
    }

    public static enum ComparisonOperator {
        LESS_THAN((n1, n2) -> {
            return n1.doubleValue() < n2.doubleValue();
        }),
        GREATER_THAN((n1, n2) -> {
            return n1.doubleValue() > n2.doubleValue();
        }),
        LESS_THAN_OR_EQUAL_TO((n1, n2) -> {
            return n1.doubleValue() <= n2.doubleValue();
        }),
        GREATER_THAN_OR_EQUAL_TO((n1, n2) -> {
            return n1.doubleValue() >= n2.doubleValue();
        });

        private final BiPredicate<Number, Number> predicate;

        public boolean run(Number number1, Number number2) {
            return this.predicate.test(number1, number2);
        }

        private ComparisonOperator(final BiPredicate predicate) {
            this.predicate = predicate;
        }
    }

    public static class MinMaxResult<T> {
        private final T object;
        private final Number value;

        public int getInteger() {
            return this.value.intValue();
        }

        public double getDouble() {
            return this.value.doubleValue();
        }

        public float getFloat() {
            return this.value.floatValue();
        }

        public byte getByte() {
            return this.value.byteValue();
        }

        public short getShort() {
            return this.value.shortValue();
        }

        public long getLong() {
            return this.value.longValue();
        }

        public T getObject() {
            return this.object;
        }

        public Number getValue() {
            return this.value;
        }

        public boolean equals(final Object o) {
            if (o == this) {
                return true;
            } else if (!(o instanceof MinMaxResult)) {
                return false;
            } else {
                MinMaxResult<?> other = (MinMaxResult)o;
                if (!other.canEqual(this)) {
                    return false;
                } else {
                    Object this$object = this.getObject();
                    Object other$object = other.getObject();
                    if (this$object == null) {
                        if (other$object != null) {
                            return false;
                        }
                    } else if (!this$object.equals(other$object)) {
                        return false;
                    }

                    Object this$value = this.getValue();
                    Object other$value = other.getValue();
                    if (this$value == null) {
                        if (other$value != null) {
                            return false;
                        }
                    } else if (!this$value.equals(other$value)) {
                        return false;
                    }

                    return true;
                }
            }
        }

        protected boolean canEqual(final Object other) {
            return other instanceof MinMaxResult;
        }

        public int hashCode() {
            int PRIME = true;
            int result = 1;
            Object $object = this.getObject();
            result = result * 59 + ($object == null ? 43 : $object.hashCode());
            Object $value = this.getValue();
            result = result * 59 + ($value == null ? 43 : $value.hashCode());
            return result;
        }

        public String toString() {
            Object var10000 = this.getObject();
            return "Utils.MinMaxResult(object=" + var10000 + ", value=" + this.getValue() + ")";
        }

        public MinMaxResult(final T object, final Number value) {
            this.object = object;
            this.value = value;
        }
    }

    public static enum ArithmeticOperator {
        ADD((n1, n2) -> {
            return n1.doubleValue() + n2.doubleValue();
        }),
        SUBTRACT((n1, n2) -> {
            return n1.doubleValue() - n2.doubleValue();
        }),
        MULTIPLY((n1, n2) -> {
            return n1.doubleValue() * n2.doubleValue();
        }),
        DIVIDE((n1, n2) -> {
            return n1.doubleValue() / n2.doubleValue();
        }),
        POWER((n1, n2) -> {
            return Math.pow(n1.doubleValue(), n2.doubleValue());
        });

        private final BiFunction<Number, Number, Number> function;

        public Number run(Number number1, Number number2) {
            return (Number)this.function.apply(number1, number2);
        }

        private ArithmeticOperator(final BiFunction function) {
            this.function = function;
        }
    }
}
