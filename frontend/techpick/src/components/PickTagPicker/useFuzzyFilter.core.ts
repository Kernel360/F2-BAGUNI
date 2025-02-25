/**
 * 연속된 문자 매칭 시 총 점수를 1로 설정합니다.
 *
 * 최상의 경우: 문자가 매칭되었고, 문자열의 시작이거나 이전 문자도 매칭된 경우.
 */
const SCORE_CONTINUE_MATCH = 1;
/**
 * 단어의 시작 부분에서 새로운 매칭이 발생하면 더 높은 점수를 부여합니다.
 *
 * 사용자가 단어의 시작 부분을 입력할 가능성이 높기 때문입니다.
 *
 * 참고: 공백을 기준으로 한 단어 이동은 슬래시, 괄호, 하이픈 등보다 높은 점수를 받습니다.
 */
const SCORE_SPACE_WORD_JUMP = 0.9;
const SCORE_NON_SPACE_WORD_JUMP = 0.8;
/**
 * 기타 매칭은 이상적이지 않지만, 완전성을 위해 포함됩니다.
 */
const SCORE_CHARACTER_JUMP = 0.17;
/**
 * 두 문자가 전치(transposition)된 경우 상당한 페널티를 부여합니다.
 *
 * 예: "uc"를 입력했을 때 "ouch"가 "curtain"보다 더 가능성이 높도록 만듭니다.
 */
const SCORE_TRANSPOSITION = 0.1;
/**
 * 일치하는 문자 사이에 누락된 문자가 많을수록 점수가 감소합니다.
 *
 * 예: "bd"를 입력했을 때 "bad"가 "bard"보다 더 가능성이 높도록 만듭니다.
 */
const PENALTY_SKIPPED = 0.999;
/**
 * 대소문자 정확히 일치하는 경우 약간 높은 점수를 부여합니다.
 *
 * 예: "HM"을 입력했을 때 "HTML"이 "haml"보다 더 가능성이 높도록 만듭니다.
 */
const PENALTY_CASE_MISMATCH = 0.9999;

/**
 * 사용자가 입력한 문자보다 단어가 더 길면 약간의 페널티를 부여합니다.
 *
 * 예: "html"을 입력했을 때 "html"이 "html5"보다 더 가능성이 높도록 만듭니다.
 */
const PENALTY_NOT_COMPLETE = 0.99;

const IS_GAP_REGEXP = /[\\\/_+.#"@\[\(\{&]/;
const COUNT_GAPS_REGEXP = /[\\\/_+.#"@\[\(\{&]/g;
const IS_SPACE_REGEXP = /[\s-]/;
const COUNT_SPACE_REGEXP = /[\s-]/g;

function commandScoreInner(
  string: string,
  abbreviation: string,
  lowerString: string,
  lowerAbbreviation: string,
  stringIndex: number,
  abbreviationIndex: number,
  memoizedResults: { [key: string]: number },
) {
  if (abbreviationIndex === abbreviation.length) {
    if (stringIndex === string.length) {
      return SCORE_CONTINUE_MATCH;
    }
    return PENALTY_NOT_COMPLETE;
  }

  const memoizeKey = `${stringIndex},${abbreviationIndex}`;
  if (memoizedResults[memoizeKey] !== undefined) {
    return memoizedResults[memoizeKey];
  }

  const abbreviationChar = lowerAbbreviation.charAt(abbreviationIndex);
  let index = lowerString.indexOf(abbreviationChar, stringIndex);
  let highScore = 0;

  let score: number;
  let transposedScore: number;
  let wordBreaks: RegExpMatchArray | null;
  let spaceBreaks: RegExpMatchArray | null;

  while (index >= 0) {
    score = commandScoreInner(
      string,
      abbreviation,
      lowerString,
      lowerAbbreviation,
      index + 1,
      abbreviationIndex + 1,
      memoizedResults,
    );
    if (score > highScore) {
      if (index === stringIndex) {
        score *= SCORE_CONTINUE_MATCH;
      } else if (IS_GAP_REGEXP.test(string.charAt(index - 1))) {
        score *= SCORE_NON_SPACE_WORD_JUMP;
        wordBreaks = string
          .slice(stringIndex, index - 1)
          .match(COUNT_GAPS_REGEXP);

        if (wordBreaks && stringIndex > 0) {
          score *= PENALTY_SKIPPED ** wordBreaks.length;
        }
      } else if (IS_SPACE_REGEXP.test(string.charAt(index - 1))) {
        score *= SCORE_SPACE_WORD_JUMP;
        spaceBreaks = string
          .slice(stringIndex, index - 1)
          .match(COUNT_SPACE_REGEXP);
        if (spaceBreaks && stringIndex > 0) {
          score *= PENALTY_SKIPPED ** spaceBreaks.length;
        }
      } else {
        score *= SCORE_CHARACTER_JUMP;
        if (stringIndex > 0) {
          score *= PENALTY_SKIPPED ** (index - stringIndex);
        }
      }

      if (string.charAt(index) !== abbreviation.charAt(abbreviationIndex)) {
        score *= PENALTY_CASE_MISMATCH;
      }
    }

    if (
      (score < SCORE_TRANSPOSITION &&
        lowerString.charAt(index - 1) ===
          lowerAbbreviation.charAt(abbreviationIndex + 1)) ||
      (lowerAbbreviation.charAt(abbreviationIndex + 1) ===
        lowerAbbreviation.charAt(abbreviationIndex) && // allow duplicate letters. Ref #7428
        lowerString.charAt(index - 1) !==
          lowerAbbreviation.charAt(abbreviationIndex))
    ) {
      transposedScore = commandScoreInner(
        string,
        abbreviation,
        lowerString,
        lowerAbbreviation,
        index + 1,
        abbreviationIndex + 2,
        memoizedResults,
      );

      if (transposedScore * SCORE_TRANSPOSITION > score) {
        score = transposedScore * SCORE_TRANSPOSITION;
      }
    }

    if (score > highScore) {
      highScore = score;
    }

    index = lowerString.indexOf(abbreviationChar, index + 1);
  }

  memoizedResults[memoizeKey] = highScore;
  return highScore;
}

function formatInput(string: string) {
  // convert all valid space characters to space so they match each other
  return string.toLowerCase().replace(COUNT_SPACE_REGEXP, ' ');
}

export function commandScore(
  string: string,
  abbreviation: string,
  aliases: string[],
): number {
  const newString =
    aliases && aliases.length > 0
      ? `${`${string} ${aliases.join(' ')}`}`
      : string;
  return commandScoreInner(
    newString,
    abbreviation,
    formatInput(newString),
    formatInput(abbreviation),
    0,
    0,
    {},
  );
}
