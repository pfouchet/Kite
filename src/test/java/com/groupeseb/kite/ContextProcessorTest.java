package com.groupeseb.kite;

import com.groupeseb.kite.function.Function;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.testng.annotations.Test;

import java.text.ParseException;
import java.util.Collections;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

public class ContextProcessorTest {

	@Test
	public void testTimestamp() throws ParseException {
		ContextProcessor contextProcessor = new ContextProcessor(Collections.<Function>emptyList(), new KiteContext());
		String timestampPlaceHolder = "{{Timestamp:Now}}";
		String leftString = "poiuyte";
		String rightString = "mmmmmmpolkiu";
		String result = contextProcessor.applyFunctions(leftString + timestampPlaceHolder + rightString, true);
		result = result.replace(leftString, "").replace(rightString, "");
		Date date = DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT.parse(result);
		assertThat(date).isBeforeOrEqualsTo(new Date());

	}

}