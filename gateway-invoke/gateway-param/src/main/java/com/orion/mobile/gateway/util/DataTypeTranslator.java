package com.orion.mobile.gateway.util;

import com.google.common.collect.Maps;
import com.orion.logger.BusinessLoggerFactory;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

public class DataTypeTranslator {
	private static final Logger log = BusinessLoggerFactory.getBusinessLogger(DataTypeTranslator.class);
	// QqAw1eZrWt2ySuXi3oEpDa4sCdRf5gFhVj6kTlGz7xBcYv8bHnNm9UJ0MI_KO$LP // base 64
	private static final String BIG_ENDIAN_RAINBOW = "qa9zw2sx8ed7cr1fv5tg4by3hn6uj0mi"; // base 32
	private static final char[] rainbowChars = BIG_ENDIAN_RAINBOW.toCharArray();
	private static final char[] bonusChars = {'k', 'o', 'l', 'p'}; // bonus chars are added for complexity and fun

	private static final Map<String, Class> PRIMITIVE_TYPES = Maps.newConcurrentMap();
	private static final Map<String, Class> PRIMITIVE_ARRAY_TYPES = Maps.newConcurrentMap();
	private static final Map<String, Class> COLLECTION_TYPES = Maps.newConcurrentMap();
	private static final Map<String, Class> MAP_TYPES = Maps.newConcurrentMap();
	private static final Map<String, Function<String, Object>> STRING_CAST_TO_PRIMITIVE_FUNCTIONS = Maps
			.newConcurrentMap();
	private static final Function<String, Object> STRING_TO_STRING_ARRAY = s -> {
		String[] result = StringUtils.split(StringUtils.removeEnd(StringUtils.removeStart(s, "["), "]"), ",");
		if (result != null && result.length > 0) {
			for (int i = 0; i < result.length; i++) {
				result[i] = StringUtils.trim(result[i]);
			}
		}
		return result;
	};
	private static final Function<String, Object> STRING_TO_BOOLEAN_ARRAY = s -> {
		Boolean[] result = null;
		String[] strings = StringUtils.split(StringUtils.removeEnd(StringUtils.removeStart(s, "["), "]"), ",");
		if (strings != null && strings.length > 0) {
			result = new Boolean[strings.length];
			for (int i = 0; i < strings.length; i++) {
				result[i] = Boolean.valueOf(StringUtils.trim(strings[i]));
			}
		}
		return result;
	};
	private static final Function<String, Object> STRING_TO_BOOLEAN_ARRAY_UNBOXED = s -> {
		boolean[] result = null;
		String[] strings = StringUtils.split(StringUtils.removeEnd(StringUtils.removeStart(s, "["), "]"), ",");
		if (strings != null && strings.length > 0) {
			result = new boolean[strings.length];
			for (int i = 0; i < strings.length; i++) {
				result[i] = Boolean.parseBoolean(StringUtils.trim(strings[i]));
			}
		}
		return result;
	};
	private static final Function<String, Object> STRING_TO_BYTE_ARRAY = s -> {
		Byte[] result = null;
		String[] strings = StringUtils.split(StringUtils.removeEnd(StringUtils.removeStart(s, "["), "]"), ",");
		if (strings != null && strings.length > 0) {
			result = new Byte[strings.length];
			for (int i = 0; i < strings.length; i++) {
				result[i] = Byte.valueOf(StringUtils.trim(strings[i]));
			}
		}
		return result;
	};
	private static final Function<String, Object> STRING_TO_BYTE_ARRAY_UNBOXED = s -> {
		byte[] result = null;
		String[] strings = StringUtils.split(StringUtils.removeEnd(StringUtils.removeStart(s, "["), "]"), ",");
		if (strings != null && strings.length > 0) {
			result = new byte[strings.length];
			for (int i = 0; i < strings.length; i++) {
				result[i] = Byte.parseByte(StringUtils.trim(strings[i]));
			}
		}
		return result;
	};
	private static final Function<String, Object> STRING_TO_SHORT_ARRAY = s -> {
		Short[] result = null;
		String[] strings = StringUtils.split(StringUtils.removeEnd(StringUtils.removeStart(s, "["), "]"), ",");
		if (strings != null && strings.length > 0) {
			result = new Short[strings.length];
			for (int i = 0; i < strings.length; i++) {
				result[i] = Short.valueOf(StringUtils.trim(strings[i]));
			}
		}
		return result;
	};
	private static final Function<String, Object> STRING_TO_SHORT_ARRAY_UNBOXED = s -> {
		short[] result = null;
		String[] strings = StringUtils.split(StringUtils.removeEnd(StringUtils.removeStart(s, "["), "]"), ",");
		if (strings != null && strings.length > 0) {
			result = new short[strings.length];
			for (int i = 0; i < strings.length; i++) {
				result[i] = Short.parseShort(StringUtils.trim(strings[i]));
			}
		}
		return result;
	};
	private static final Function<String, Object> STRING_TO_INTEGER_ARRAY = s -> {
		Integer[] result = null;
		String[] strings = StringUtils.split(StringUtils.removeEnd(StringUtils.removeStart(s, "["), "]"), ",");
		if (strings != null && strings.length > 0) {
			result = new Integer[strings.length];
			for (int i = 0; i < strings.length; i++) {
				result[i] = Integer.valueOf(StringUtils.trim(strings[i]));
			}
		}
		return result;
	};
	private static final Function<String, Object> STRING_TO_INTEGER_ARRAY_UNBOXED = s -> {
		int[] result = null;
		String[] strings = StringUtils.split(StringUtils.removeEnd(StringUtils.removeStart(s, "["), "]"), ",");
		if (strings != null && strings.length > 0) {
			result = new int[strings.length];
			for (int i = 0; i < strings.length; i++) {
				result[i] = Integer.parseInt(StringUtils.trim(strings[i]));
			}
		}
		return result;
	};
	private static final Function<String, Object> STRING_TO_LONG_ARRAY = s -> {
		Long[] result = null;
		String[] strings = StringUtils.split(StringUtils.removeEnd(StringUtils.removeStart(s, "["), "]"), ",");
		if (strings != null && strings.length > 0) {
			result = new Long[strings.length];
			for (int i = 0; i < strings.length; i++) {
				result[i] = Long.valueOf(StringUtils.trim(strings[i]));
			}
		}
		return result;
	};
	private static final Function<String, Object> STRING_TO_LONG_ARRAY_UNBOXED = s -> {
		long[] result = null;
		String[] strings = StringUtils.split(StringUtils.removeEnd(StringUtils.removeStart(s, "["), "]"), ",");
		if (strings != null && strings.length > 0) {
			result = new long[strings.length];
			for (int i = 0; i < strings.length; i++) {
				result[i] = Long.parseLong(StringUtils.trim(strings[i]));
			}
		}
		return result;
	};
	private static final Function<String, Object> STRING_TO_FLOAT_ARRAY = s -> {
		Float[] result = null;
		String[] strings = StringUtils.split(StringUtils.removeEnd(StringUtils.removeStart(s, "["), "]"), ",");
		if (strings != null && strings.length > 0) {
			result = new Float[strings.length];
			for (int i = 0; i < strings.length; i++) {
				result[i] = Float.valueOf(StringUtils.trim(strings[i]));
			}
		}
		return result;
	};
	private static final Function<String, Object> STRING_TO_FLOAT_ARRAY_UNBOXED = s -> {
		float[] result = null;
		String[] strings = StringUtils.split(StringUtils.removeEnd(StringUtils.removeStart(s, "["), "]"), ",");
		if (strings != null && strings.length > 0) {
			result = new float[strings.length];
			for (int i = 0; i < strings.length; i++) {
				result[i] = Float.parseFloat(StringUtils.trim(strings[i]));
			}
		}
		return result;
	};
	private static final Function<String, Object> STRING_TO_DOUBLE_ARRAY = s -> {
		Double[] result = null;
		String[] strings = StringUtils.split(StringUtils.removeEnd(StringUtils.removeStart(s, "["), "]"), ",");
		if (strings != null && strings.length > 0) {
			result = new Double[strings.length];
			for (int i = 0; i < strings.length; i++) {
				result[i] = Double.valueOf(StringUtils.trim(strings[i]));
			}
		}
		return result;
	};
	private static final Function<String, Object> STRING_TO_DOUBLE_ARRAY_UNBOXED = s -> {
		double[] result = null;
		String[] strings = StringUtils.split(StringUtils.removeEnd(StringUtils.removeStart(s, "["), "]"), ",");
		if (strings != null && strings.length > 0) {
			result = new double[strings.length];
			for (int i = 0; i < strings.length; i++) {
				result[i] = Double.parseDouble(StringUtils.trim(strings[i]));
			}
		}
		return result;
	};

	static {
		// string
		PRIMITIVE_TYPES.put("java.lang.String", String.class);
		PRIMITIVE_TYPES.put("String", String.class);
		PRIMITIVE_TYPES.put("string", String.class);
		PRIMITIVE_ARRAY_TYPES.put("java.lang.String[]", String[].class);
		PRIMITIVE_ARRAY_TYPES.put("String[]", String[].class);
		PRIMITIVE_ARRAY_TYPES.put("string[]", String[].class);
		STRING_CAST_TO_PRIMITIVE_FUNCTIONS.put("java.lang.String", String::valueOf);
		STRING_CAST_TO_PRIMITIVE_FUNCTIONS.put("String", String::valueOf);
		STRING_CAST_TO_PRIMITIVE_FUNCTIONS.put("string", String::valueOf);
		STRING_CAST_TO_PRIMITIVE_FUNCTIONS.put("java.lang.String[]", STRING_TO_STRING_ARRAY);
		STRING_CAST_TO_PRIMITIVE_FUNCTIONS.put("String[]", STRING_TO_STRING_ARRAY);
		STRING_CAST_TO_PRIMITIVE_FUNCTIONS.put("string[]", STRING_TO_STRING_ARRAY);

		// boolean
		PRIMITIVE_TYPES.put("java.lang.Boolean", Boolean.class);
		PRIMITIVE_TYPES.put("Boolean", Boolean.class);
		PRIMITIVE_TYPES.put("boolean", boolean.class);
		PRIMITIVE_ARRAY_TYPES.put("java.lang.Boolean[]", Boolean[].class);
		PRIMITIVE_ARRAY_TYPES.put("Boolean[]", Boolean[].class);
		PRIMITIVE_ARRAY_TYPES.put("boolean[]", boolean[].class);
		STRING_CAST_TO_PRIMITIVE_FUNCTIONS.put("java.lang.Boolean", Boolean::valueOf);
		STRING_CAST_TO_PRIMITIVE_FUNCTIONS.put("Boolean", Boolean::valueOf);
		STRING_CAST_TO_PRIMITIVE_FUNCTIONS.put("boolean", Boolean::parseBoolean);
		STRING_CAST_TO_PRIMITIVE_FUNCTIONS.put("java.lang.Boolean[]", STRING_TO_BOOLEAN_ARRAY);
		STRING_CAST_TO_PRIMITIVE_FUNCTIONS.put("Boolean[]", STRING_TO_BOOLEAN_ARRAY);
		STRING_CAST_TO_PRIMITIVE_FUNCTIONS.put("boolean[]", STRING_TO_BOOLEAN_ARRAY_UNBOXED);

		// byte
		PRIMITIVE_TYPES.put("java.lang.Byte", Byte.class);
		PRIMITIVE_TYPES.put("Byte", Byte.class);
		PRIMITIVE_TYPES.put("byte", byte.class);
		PRIMITIVE_ARRAY_TYPES.put("java.lang.Byte[]", Byte[].class);
		PRIMITIVE_ARRAY_TYPES.put("Byte[]", Byte[].class);
		PRIMITIVE_ARRAY_TYPES.put("byte[]", byte[].class);
		STRING_CAST_TO_PRIMITIVE_FUNCTIONS.put("java.lang.Byte", Byte::valueOf);
		STRING_CAST_TO_PRIMITIVE_FUNCTIONS.put("Byte", Byte::valueOf);
		STRING_CAST_TO_PRIMITIVE_FUNCTIONS.put("byte", Byte::parseByte);
		STRING_CAST_TO_PRIMITIVE_FUNCTIONS.put("java.lang.Byte[]", STRING_TO_BYTE_ARRAY);
		STRING_CAST_TO_PRIMITIVE_FUNCTIONS.put("Byte[]", STRING_TO_BYTE_ARRAY);
		STRING_CAST_TO_PRIMITIVE_FUNCTIONS.put("byte[]", STRING_TO_BYTE_ARRAY_UNBOXED);

		// short
		PRIMITIVE_TYPES.put("java.lang.Short", Short.class);
		PRIMITIVE_TYPES.put("Short", Short.class);
		PRIMITIVE_TYPES.put("short", short.class);
		PRIMITIVE_ARRAY_TYPES.put("java.lang.Short[]", Short[].class);
		PRIMITIVE_ARRAY_TYPES.put("Short[]", Short.class);
		PRIMITIVE_ARRAY_TYPES.put("short[]", short.class);
		STRING_CAST_TO_PRIMITIVE_FUNCTIONS.put("java.lang.Short", Short::valueOf);
		STRING_CAST_TO_PRIMITIVE_FUNCTIONS.put("Short", Short::valueOf);
		STRING_CAST_TO_PRIMITIVE_FUNCTIONS.put("short", Short::parseShort);
		STRING_CAST_TO_PRIMITIVE_FUNCTIONS.put("java.lang.Short[]", STRING_TO_SHORT_ARRAY);
		STRING_CAST_TO_PRIMITIVE_FUNCTIONS.put("Short[]", STRING_TO_SHORT_ARRAY);
		STRING_CAST_TO_PRIMITIVE_FUNCTIONS.put("short[]", STRING_TO_SHORT_ARRAY_UNBOXED);

		// integer
		PRIMITIVE_TYPES.put("java.lang.Integer", Integer.class);
		PRIMITIVE_TYPES.put("Integer", Integer.class);
		PRIMITIVE_TYPES.put("int", int.class);
		PRIMITIVE_ARRAY_TYPES.put("java.lang.Integer[]", Integer[].class);
		PRIMITIVE_ARRAY_TYPES.put("Integer[]", Integer[].class);
		PRIMITIVE_ARRAY_TYPES.put("int[]", int[].class);
		STRING_CAST_TO_PRIMITIVE_FUNCTIONS.put("java.lang.Integer", Integer::valueOf);
		STRING_CAST_TO_PRIMITIVE_FUNCTIONS.put("Integer", Integer::valueOf);
		STRING_CAST_TO_PRIMITIVE_FUNCTIONS.put("int", Integer::parseInt);
		STRING_CAST_TO_PRIMITIVE_FUNCTIONS.put("java.lang.Integer[]", STRING_TO_INTEGER_ARRAY);
		STRING_CAST_TO_PRIMITIVE_FUNCTIONS.put("Integer[]", STRING_TO_INTEGER_ARRAY);
		STRING_CAST_TO_PRIMITIVE_FUNCTIONS.put("int[]", STRING_TO_INTEGER_ARRAY_UNBOXED);

		// long
		PRIMITIVE_TYPES.put("java.lang.Long", Long.class);
		PRIMITIVE_TYPES.put("Long", Long.class);
		PRIMITIVE_TYPES.put("long", long.class);
		PRIMITIVE_ARRAY_TYPES.put("java.lang.Long[]", Long[].class);
		PRIMITIVE_ARRAY_TYPES.put("Long[]", Long[].class);
		PRIMITIVE_ARRAY_TYPES.put("long[]", long[].class);
		STRING_CAST_TO_PRIMITIVE_FUNCTIONS.put("java.lang.Long", Long::valueOf);
		STRING_CAST_TO_PRIMITIVE_FUNCTIONS.put("Long", Long::valueOf);
		STRING_CAST_TO_PRIMITIVE_FUNCTIONS.put("long", Long::parseLong);
		STRING_CAST_TO_PRIMITIVE_FUNCTIONS.put("java.lang.Long[]", STRING_TO_LONG_ARRAY);
		STRING_CAST_TO_PRIMITIVE_FUNCTIONS.put("Long[]", STRING_TO_LONG_ARRAY);
		STRING_CAST_TO_PRIMITIVE_FUNCTIONS.put("long[]", STRING_TO_LONG_ARRAY_UNBOXED);

		// float
		PRIMITIVE_TYPES.put("java.lang.Float", Float.class);
		PRIMITIVE_TYPES.put("Float", Float.class);
		PRIMITIVE_TYPES.put("float", float.class);
		PRIMITIVE_ARRAY_TYPES.put("java.lang.Float[]", Float[].class);
		PRIMITIVE_ARRAY_TYPES.put("Float[]", Float[].class);
		PRIMITIVE_ARRAY_TYPES.put("float[]", float[].class);
		STRING_CAST_TO_PRIMITIVE_FUNCTIONS.put("java.lang.Float", Float::valueOf);
		STRING_CAST_TO_PRIMITIVE_FUNCTIONS.put("Float", Float::valueOf);
		STRING_CAST_TO_PRIMITIVE_FUNCTIONS.put("float", Float::parseFloat);
		STRING_CAST_TO_PRIMITIVE_FUNCTIONS.put("java.lang.Float[]", STRING_TO_FLOAT_ARRAY);
		STRING_CAST_TO_PRIMITIVE_FUNCTIONS.put("Float[]", STRING_TO_FLOAT_ARRAY);
		STRING_CAST_TO_PRIMITIVE_FUNCTIONS.put("float[]", STRING_TO_FLOAT_ARRAY_UNBOXED);

		// double
		PRIMITIVE_TYPES.put("java.lang.Double", Double.class);
		PRIMITIVE_TYPES.put("Double", Double.class);
		PRIMITIVE_TYPES.put("double", double.class);
		PRIMITIVE_ARRAY_TYPES.put("java.lang.Double[]", Double[].class);
		PRIMITIVE_ARRAY_TYPES.put("Double[]", Double[].class);
		PRIMITIVE_ARRAY_TYPES.put("double[]", double[].class);
		STRING_CAST_TO_PRIMITIVE_FUNCTIONS.put("java.lang.Double", Double::valueOf);
		STRING_CAST_TO_PRIMITIVE_FUNCTIONS.put("Double", Double::valueOf);
		STRING_CAST_TO_PRIMITIVE_FUNCTIONS.put("double", Double::parseDouble);
		STRING_CAST_TO_PRIMITIVE_FUNCTIONS.put("java.lang.Double[]", STRING_TO_DOUBLE_ARRAY);
		STRING_CAST_TO_PRIMITIVE_FUNCTIONS.put("Double[]", STRING_TO_DOUBLE_ARRAY);
		STRING_CAST_TO_PRIMITIVE_FUNCTIONS.put("double[]", STRING_TO_DOUBLE_ARRAY_UNBOXED);

		// collections
		COLLECTION_TYPES.put("List", List.class);
		COLLECTION_TYPES.put("java.util.List", List.class);
		COLLECTION_TYPES.put("Set", Set.class);
		COLLECTION_TYPES.put("java.util.Set", Set.class);
		COLLECTION_TYPES.put("Queue", Queue.class);
		COLLECTION_TYPES.put("java.util.Queue", Queue.class);
		COLLECTION_TYPES.put("Stack", Stack.class);
		COLLECTION_TYPES.put("java.util.Stack", Stack.class);

		// map
		MAP_TYPES.put("Map", Map.class);
		MAP_TYPES.put("java.util.Map", Map.class);
	}

	/**
	 * 去掉范型参数：<br/>
	 * eg. java.util.List<java.lang.String> -> java.util.List
	 *
	 * @param types
	 * @return
	 */
	public static String[] removeGenericTypeParameter(String[] types) {
		String[] result = null;
		if (types != null && types.length > 0) {
			result = new String[types.length];
			for (int i = 0; i < types.length; i++) {
				String type = types[i];
				type = RegExUtils.replaceAll(type, "<.*>", "");
				result[i] = type;
			}
		}
		return result;
	}

	/**
	 * 补全类型：<br/>
	 * eg. List -> java.util.List
	 *
	 * @param types
	 * @return
	 */
	public static String[] completeTypes(String[] types) {
		String[] result = null;
		if (types != null && types.length > 0) {
			result = new String[types.length];
			for (int i = 0; i < types.length; i++) {
				String type = types[i];
				Map.Entry<String, Class> e;
				if (PRIMITIVE_TYPES.containsKey(type)) {
					type = PRIMITIVE_TYPES.get(type).getName();
				} else if (PRIMITIVE_ARRAY_TYPES.containsKey(type)) {
					type = PRIMITIVE_ARRAY_TYPES.get(type).getName();
				} else if ((e = findCollection(type)) != null || (e = findMap(type)) != null) {
					type = RegExUtils.replaceFirst(type, e.getKey(), e.getValue().getName());
				}
				result[i] = type;
			}
		}
		return result;
	}

	private static Map.Entry<String, Class> findMap(String type) {
		Set<Map.Entry<String, Class>> entrySet = MAP_TYPES.entrySet();
		for (Map.Entry<String, Class> entry : entrySet) {
			String t = entry.getKey();
			if (type.startsWith(t)) {
				return entry;
			}
		}
		return null;
	}

	private static Map.Entry<String, Class> findCollection(String type) {
		Set<Map.Entry<String, Class>> entrySet = COLLECTION_TYPES.entrySet();
		for (Map.Entry<String, Class> entry : entrySet) {
			String t = entry.getKey();
			if (type.startsWith(t)) {
				return entry;
			}
		}
		return null;
	}

	public static boolean isJsonArray(String json) {
		return StringUtils.startsWith(StringUtils.trim(json), "[") && StringUtils.endsWith(StringUtils.trim(json), "]");
	}

	public static Map<String, Class> getPrimitiveTypes() {
		return Collections.unmodifiableMap(PRIMITIVE_TYPES);
	}

	public static Map<String, Class> getPrimitiveArrayTypes() {
		return Collections.unmodifiableMap(PRIMITIVE_ARRAY_TYPES);
	}

	public static Map<String, Class> getCollectionTypes() {
		return Collections.unmodifiableMap(COLLECTION_TYPES);
	}

	public static Map<String, Class> getMapTypes() {
		return Collections.unmodifiableMap(MAP_TYPES);
	}

	public static Map<String, Function<String, Object>> getStringCastToPrimitiveFunctions() {
		return Collections.unmodifiableMap(STRING_CAST_TO_PRIMITIVE_FUNCTIONS);
	}

	/**
	 * 是否基本类型（包括其包装类型）
	 *
	 * @param fieldType
	 * @return
	 */
	public static boolean isPrimitive(String fieldType) {
		return DataTypeTranslator.getPrimitiveTypes().containsKey(fieldType);
	}

	/**
	 * 是否基本类型（包括其包装类型）的一维数组
	 *
	 * @param fieldType
	 * @return
	 */
	public static boolean isPrimitiveArray(String fieldType) {
		return DataTypeTranslator.getPrimitiveArrayTypes().containsKey(fieldType);
	}

	/**
	 * 是否日期类型
	 *
	 * @param fieldType
	 * @return
	 */
	public static boolean isDate(String fieldType) {
		return StringUtils.equals(fieldType, "Date") || StringUtils.equals(fieldType, "java.util.Date");
	}

	/**
	 * 是否集合类型（List|Set|Queue|Stack）
	 *
	 * @param fieldType
	 * @return
	 */
	public static boolean isCollection(String fieldType) {
		if (StringUtils.contains(fieldType, "<")) {
			fieldType = StringUtils.substring(fieldType, 0, StringUtils.indexOf(fieldType, "<"));
		}
		return DataTypeTranslator.getCollectionTypes().containsKey(fieldType);
	}

	/**
	 * 是否Map类型
	 *
	 * @param fieldType
	 * @return
	 */
	public static boolean isMap(String fieldType) {
		if (StringUtils.contains(fieldType, "<")) {
			fieldType = StringUtils.substring(fieldType, 0, StringUtils.indexOf(fieldType, "<"));
		}
		return DataTypeTranslator.getMapTypes().containsKey(fieldType);
	}

	/**
	 * 是否数组类型
	 *
	 * @param fieldType
	 * @return
	 */
	public static boolean isArray(String fieldType) {
		return StringUtils.endsWith(fieldType, "[]");
	}

	public static String removeCollectionParameterizedType(String type) {
		return StringUtils.substring(type, 0, StringUtils.indexOf(type, "<"));
	}

	/**
	 * 剥去最外层得到里面第一层泛化类型<br/>
	 * eg. java.util.List<java.util.Set<String>> -> java.util.Set<String>
	 *
	 * @param type
	 * @return
	 */
	public static String extractCollectionParameterizedType(String type) {
		// 剥去最外层得到里面第一层泛化类型
		return StringUtils.substring(type, StringUtils.indexOf(type, "<") + 1, type.length() - 1);
	}

	/**
	 * 将传入的数组类型降一个维度
	 *
	 * @param type
	 * @return
	 */
	public static String reduceArrayDimension(String type) {
		return StringUtils.removeEnd(type, "[]");
	}

	/**
	 * 扩权，转换为32进制
	 *
	 * @param nextId
	 * @return
	 */
	public static String longToBase32(long nextId) {
		StringBuilder sb = new StringBuilder();
		BigInteger id = BigInteger.valueOf(nextId);
		BigInteger zero = BigInteger.valueOf(0);
		BigInteger rainbow = BigInteger.valueOf(BIG_ENDIAN_RAINBOW.length());
		BigInteger quotient;
		BigInteger remainder;
		while ((quotient = id.divide(rainbow)).compareTo(zero) > 0) {
			remainder = id.mod(rainbow);
			randomAppendIfZero(sb, remainder);
			id = quotient;
		}
		randomAppendIfZero(sb, id);
		return sb.reverse().toString();
	}

	/**
	 * snowflake的id从0开始发放，并发低的时候结尾字符会重复，使用bonusChars随机多样化
	 *
	 * @param sb
	 * @param bigInteger
	 */
	private static void randomAppendIfZero(StringBuilder sb, BigInteger bigInteger) {
		if (bigInteger.compareTo(BigInteger.valueOf(0)) == 0
				&& ThreadLocalRandom.current().nextInt(bonusChars.length) > 1) {
			sb.append(bonusChars[ThreadLocalRandom.current().nextInt(bonusChars.length)]);
		} else {
			sb.append(rainbowChars[bigInteger.mod(BigInteger.valueOf(rainbowChars.length)).intValue()]);
		}
	}

	/**
	 * 32进制转换回十进制
	 *
	 * @param hex
	 * @return
	 */
	public static long base32ToLong(String hex) {
		BigInteger result = BigInteger.valueOf(0);
		BigInteger radix = BigInteger.valueOf(BIG_ENDIAN_RAINBOW.length());
		for (int i = 0; i < hex.length(); i++) {
			char c = hex.charAt(hex.length() - 1 - i);
			int index;
			if (ArrayUtils.contains(bonusChars, c)) {
				index = 0;
			} else {
				index = ArrayUtils.indexOf(rainbowChars, c);
			}
			BigInteger b = BigInteger.valueOf(index);
			result = result.add(b.multiply(radix.pow(i)));
		}
		return result.longValue();
	}

	@SuppressWarnings("unchecked")
	public static <T> T readAndCastField(Object target, String fieldName, boolean forceAccess) {
		if (target == null) {
			throw new IllegalArgumentException("target object cannot be null!");
		}
		if (StringUtils.isBlank(fieldName)) {
			throw new IllegalArgumentException("field name cannot be blank!");
		}
		T result = null;
		Optional<Field> fieldOptional = Arrays.stream(target.getClass().getDeclaredFields())
				.filter(f -> StringUtils.equals(f.getName(), fieldName)).findFirst();
		if (fieldOptional.isPresent()) {
			Field f = fieldOptional.get();
			f.setAccessible(forceAccess);
			try {
				Object value = f.get(target);
				if (value != null) {
					result = (T) value;
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
		return result;
	}
}
