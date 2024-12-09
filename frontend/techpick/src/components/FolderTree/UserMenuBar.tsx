import { useEffect } from 'react';
import Link from 'next/link';
import { CircleUserRoundIcon, SearchIcon } from 'lucide-react';
import { ROUTES } from '@/constants';
import { useDisclosure } from '@/hooks';
import { useSearchPickStore } from '@/stores/searchPickStore';
import {
  userMenuBarLayoutStyle,
  myPageLinkStyle,
  searchButtonStyle,
} from './userMenuBar.css';
import SearchDialog from '../Search2/SearchDialog';

export function UserMenuBar() {
  const { searchPicksByQueryParam } = useSearchPickStore();
  const {
    isOpen: isSearchDialogOpen,
    onOpen: onSearchDialogOpen,
    onClose: onSearchDialogClose,
  } = useDisclosure();

  useEffect(() => {
    searchPicksByQueryParam();
  }, []);

  return (
    <div className={userMenuBarLayoutStyle}>
      <Link href={ROUTES.MY_PAGE} className={myPageLinkStyle}>
        <CircleUserRoundIcon size={20} />
      </Link>
      <button className={searchButtonStyle} onClick={onSearchDialogOpen}>
        <SearchIcon size={20} />
      </button>
      <SearchDialog
        isOpen={isSearchDialogOpen}
        onOpenChange={onSearchDialogClose}
      />
    </div>
  );
}
