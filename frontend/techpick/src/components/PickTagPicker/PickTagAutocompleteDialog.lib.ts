import { MAXIMUM_INT_RANGE } from './PickTagPicker.constants';

export const getRandomInt = () => {
  return Math.floor(Math.random() * MAXIMUM_INT_RANGE);
};
