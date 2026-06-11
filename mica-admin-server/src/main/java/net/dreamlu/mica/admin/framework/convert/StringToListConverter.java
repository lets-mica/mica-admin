package net.dreamlu.mica.admin.framework.convert;

import lombok.extern.slf4j.Slf4j;
import net.dreamlu.mica.core.utils.StringUtil;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.lang.Nullable;

import java.util.*;

/**
 * 参数转换 String -> List
 *
 * @author L.cm
 */
@Slf4j
public class StringToListConverter implements GenericConverter {

	@Override
	public Set<ConvertiblePair> getConvertibleTypes() {
		Set<ConvertiblePair> pairSet = new HashSet<>(1);
		pairSet.add(new ConvertiblePair(String.class, List.class));
		return Collections.unmodifiableSet(pairSet);
	}

	@Override
	public Object convert(@Nullable Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
		if (source == null) {
			return null;
		}
		return Arrays.asList(StringUtil.commaDelimitedListToStringArray((String) source));
	}

}
