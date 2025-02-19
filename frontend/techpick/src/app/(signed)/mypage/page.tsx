'use client';

import { postLogout } from '@/apis/postLogout';
import { FolderContentLayout } from '@/components/FolderContentLayout';
import MyPageContentContainer from '@/components/MyPage/MyPageContentContainer';
import MyPageShareFolderContent from '@/components/MyPage/MyPageShareFolderContent';
import { TutorialDialog } from '@/components/TutorialDialog';
import { ROUTES } from '@/constants/route';
import { useDisclosure } from '@/hooks/useDisclosure';
import { useUserSessionReset } from '@/libs/@eventlog/useUserSessionReset';
import * as Checkbox from '@radix-ui/react-checkbox';
import { CheckIcon } from 'lucide-react';
import {
  buttonSectionStyle,
  checkboxIndicatorStyle,
  checkboxRootStyle,
  logoutButtonStyle,
  myPageContentContainerLayoutStyle,
  myPageLayoutStyle,
  tutorialReplayCheckboxLabelStyle,
  tutorialReplayCheckboxLayoutStyle,
} from './page.css';

export default function MyPage() {
  const { isOpen, onClose, onToggle } = useDisclosure();
  const { userSessionReset } = useUserSessionReset();

  const handleLogout = async () => {
    try {
      await postLogout();
      userSessionReset();
      window.location.replace(ROUTES.LOGIN);
    } catch {
      /* empty */
    }
  };

  return (
    <FolderContentLayout>
      <div className={myPageLayoutStyle}>
        <div className={myPageContentContainerLayoutStyle}>
          <MyPageContentContainer title="내 계정">
            <div className={buttonSectionStyle}>
              {/* biome-ignore lint/a11y/useButtonType: <explanation> */}
              <button className={logoutButtonStyle} onClick={handleLogout}>
                로그아웃
              </button>
            </div>
          </MyPageContentContainer>
          <div className={tutorialReplayCheckboxLayoutStyle}>
            <Checkbox.Root
              id="tutorial-replay-checkbox"
              checked={isOpen}
              onCheckedChange={onToggle}
              className={checkboxRootStyle}
            >
              <Checkbox.Indicator className={checkboxIndicatorStyle}>
                <CheckIcon />
              </Checkbox.Indicator>
            </Checkbox.Root>
            <label
              htmlFor="tutorial-replay-checkbox"
              className={tutorialReplayCheckboxLabelStyle}
            >
              튜토리얼 다시 보기
            </label>
          </div>
          {isOpen && <TutorialDialog isOpen={isOpen} onClose={onClose} />}
        </div>

        <MyPageContentContainer title="공개된 폴더">
          <MyPageShareFolderContent />
        </MyPageContentContainer>
      </div>
    </FolderContentLayout>
  );
}
