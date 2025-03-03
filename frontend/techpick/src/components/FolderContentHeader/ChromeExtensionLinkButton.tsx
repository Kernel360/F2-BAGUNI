import { PlusIcon } from 'lucide-react';
import { chromeExtensionLinkButtonStyle } from './folderContentHeaderButton.css';

export function ChromeExtensionLinkButton() {
  return (
    <a
      href="https://chromewebstore.google.com/detail/%EB%B0%94%EA%B5%AC%EB%8B%88-%EC%9D%B5%EC%8A%A4%ED%85%90%EC%85%98/gfkkgllophliamkdclhekgfiohnbdddl"
      target="_blank"
      rel="noopener noreferrer"
    >
      <button className={chromeExtensionLinkButtonStyle} type="button">
        <PlusIcon size={12} />
        <span>북마크 추가하기</span>
      </button>
    </a>
  );
}
