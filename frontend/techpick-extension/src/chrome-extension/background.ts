import {
  GET_THEME_FROM_LOCALHOST_PORT_NAME,
  REQUEST_THEME_STATE_FROM_LOCALHOST_MESSAGE,
  LIGHT_THEME_STATE,
  DARK_THEME_STATE,
  THEME_STATE_LOCALHOST_KEY,
  CHANGE_THEME_STATE_TO_LOCALHOST_PORT_NAME,
} from '@/constants';

/**
 * @description 테마를 익스텐션 로컬 호스트에서 불러오는 함수입니다.
 */
chrome.runtime.onConnect.addListener(function checkThemeState(port) {
  if (port.name !== GET_THEME_FROM_LOCALHOST_PORT_NAME) {
    return;
  }

  port.onMessage.addListener(function (msg: string) {
    if (msg === REQUEST_THEME_STATE_FROM_LOCALHOST_MESSAGE) {
      // 로컬 스토리지에서 값 불러오기.
      chrome.storage.sync.get([THEME_STATE_LOCALHOST_KEY]).then((value) => {
        // 없다면 light를 기본 값으로 저장하기.
        if (!value[THEME_STATE_LOCALHOST_KEY]) {
          chrome.storage.sync.set({
            [THEME_STATE_LOCALHOST_KEY]: LIGHT_THEME_STATE,
          });
        }

        port.postMessage(value[THEME_STATE_LOCALHOST_KEY]);
      });
    }
  });
});

/**
 * @description 테마를 변경하는 함수입니다.
 */
chrome.runtime.onConnect.addListener(function changeThemeState(port) {
  if (port.name !== CHANGE_THEME_STATE_TO_LOCALHOST_PORT_NAME) {
    return;
  }

  port.onMessage.addListener(function (msg: string) {
    if (msg !== LIGHT_THEME_STATE && msg !== DARK_THEME_STATE) {
      return;
    }

    chrome.storage.sync.set({
      [THEME_STATE_LOCALHOST_KEY]: msg,
    });
  });
});
