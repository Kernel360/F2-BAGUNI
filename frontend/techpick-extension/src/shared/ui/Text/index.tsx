import { PropsWithChildren } from 'react';
import { Slot, Slottable } from '@radix-ui/react-slot';
import {
  fontSizeVariants,
  fontWeightVariants,
  fontSizeVariantKeyTypes,
  fontWeightVariantKeyTypes,
} from './Text.css';

export function Text({
  size = 'md',
  weight = 'regular',
  asChild,
  children,
}: PropsWithChildren<TextProps>) {
  const Component = asChild ? Slot : 'span';

  return (
    <Component
      className={`${fontSizeVariants[size]} ${fontWeightVariants[weight]}`}
    >
      <Slottable>{children}</Slottable>
    </Component>
  );
}

interface TextProps {
  size?: fontSizeVariantKeyTypes;
  weight?: fontWeightVariantKeyTypes;
  asChild?: boolean;
}
