'use client';

import { useDisclosure } from '@/hooks/useDisclosure';
import { SearchIcon } from 'lucide-react';
import SearchDialog from '../Search2/SearchDialog';
import { SearchBarDescription } from './SarchBarDescription';
import { searchItemStyle } from './searchBar.css';

export function SearchBar() {
  const { isOpen: isSearchDialogOpen, onToggle: onSearchDialogToggle } =
    useDisclosure();

  return (
    <div>
      <div
        className={searchItemStyle}
        onClick={onSearchDialogToggle}
        onKeyDown={(e) => {
          if (e.key === 'Enter') {
            onSearchDialogToggle();
          }
        }}
      >
        <SearchIcon size={16} />
        <SearchBarDescription />
      </div>

      <SearchDialog
        isOpen={isSearchDialogOpen}
        onOpenChange={onSearchDialogToggle}
      />
    </div>
  );
}
