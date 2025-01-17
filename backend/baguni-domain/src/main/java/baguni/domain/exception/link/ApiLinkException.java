package baguni.domain.exception.link;

import baguni.common.exception.base.ApiErrorCode;
import baguni.common.exception.base.ApiException;

public class ApiLinkException extends ApiException {

	private ApiLinkException(ApiErrorCode errorCode) {
		super(errorCode);
	}

	/**
	 * TODO: Implement static factory method
	 */
	public static ApiLinkException LINK_NOT_FOUND() {
		return new ApiLinkException(ApiLinkErrorCode.LINK_NOT_FOUND);
	}

	public static ApiLinkException LINK_HAS_PICKS() {
		return new ApiLinkException(ApiLinkErrorCode.LINK_HAS_PICKS);
	}

	public static ApiLinkException LINK_ALREADY_EXISTS() {
		return new ApiLinkException(ApiLinkErrorCode.LINK_ALREADY_EXIST);
	}

	public static ApiLinkException LINK_OG_TAG_UPDATE_FAILURE() {
		return new ApiLinkException(ApiLinkErrorCode.LINK_OG_TAG_UPDATE_FAILURE);
	}

	public static ApiLinkException LINK_URL_TOO_LONG() {
		return new ApiLinkException(ApiLinkErrorCode.LINK_URL_TOO_LONG);
	}
}
