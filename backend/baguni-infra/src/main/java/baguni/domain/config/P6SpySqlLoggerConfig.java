package baguni.domain.config;

import static org.springframework.util.StringUtils.*;

import java.sql.SQLException;
import java.util.Locale;

import org.hibernate.engine.jdbc.internal.FormatStyle;
import org.springframework.stereotype.Component;

import com.p6spy.engine.common.ConnectionInformation;
import com.p6spy.engine.event.JdbcEventListener;
import com.p6spy.engine.logging.Category;
import com.p6spy.engine.spy.P6SpyOptions;
import com.p6spy.engine.spy.appender.MessageFormattingStrategy;

/**
 * Jdbc가 DB Connection을 얻은 이후에 로깅 포맷을 P6SpyOptions가 가로채도록 하는 Bean입니다.
 */
@Component
public class P6SpySqlLoggerConfig extends JdbcEventListener implements MessageFormattingStrategy {

	@Override
	public void onAfterGetConnection(ConnectionInformation connectionInformation, SQLException e) {
		P6SpyOptions.getActiveInstance().setLogMessageFormat(this.getClass().getName());
	}

	@Override
	public String formatMessage(int connectionId, String now, long elapsed, String category,
		String prepared, String sql, String url) {
		return highlight(format(category, sql));
	}

	private String highlight(String sql) {
		return FormatStyle.HIGHLIGHT.getFormatter().format(sql);
	}

	private String format(String category, String sql) {
		if (hasText(sql) && isStatement(category)) {
			if (isDdl(trim(sql))) {
				return FormatStyle.DDL.getFormatter().format(sql);
			}
			return FormatStyle.BASIC.getFormatter().format(sql);
		}
		return sql;
	}

	private static boolean isDdl(String trimmedSql) {
		return trimmedSql.startsWith("create")
			|| trimmedSql.startsWith("alter")
			|| trimmedSql.startsWith("drop")
			|| trimmedSql.startsWith("comment");
	}

	private static String trim(String sql) {
		return sql.trim().toLowerCase(Locale.ROOT);
	}

	private static boolean isStatement(String category) {
		return Category.STATEMENT.getName().equals(category);
	}
}
