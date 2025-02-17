type UpdateType = 'sync' | 'microtask' | 'animationFrame' | 'default';

type ScheduleFunction = (callback: () => void) => void;

/**
 * tanstack query에서의 작업들은 비동기적으로 이뤄집니다.
 * @example
 * default: setTimeout(callback, 0)
 *
 * @description
 * UI 인터렉션이나 더 빠른 상태 변경을 위해서 만들었습니다.
 */
const createTaskScheduler = () => {
  let currentUpdateType: UpdateType = 'default';

  const taskScheduler: ScheduleFunction = (callback) => {
    const wrappedCallback = () => {
      callback();
      currentUpdateType = 'default';
    };

    switch (currentUpdateType) {
      case 'sync':
        wrappedCallback();
        break;
      case 'microtask':
        queueMicrotask(wrappedCallback);
        break;
      case 'animationFrame':
        requestAnimationFrame(wrappedCallback);
        break;
      default:
        setTimeout(wrappedCallback, 0);
    }
  };

  const setUpdateType = (type: UpdateType) => {
    currentUpdateType = type;
  };

  return { taskScheduler, setUpdateType };
};

export const { taskScheduler, setUpdateType } = createTaskScheduler();

export const syncUpdate = (callback: () => void) => {
  setUpdateType('sync');
  taskScheduler(callback);
};

export const microtaskUpdate = (callback: () => void) => {
  setUpdateType('microtask');
  taskScheduler(callback);
};

export const animationFrameUpdate = (callback: () => void) => {
  setUpdateType('animationFrame');
  taskScheduler(callback);
};
