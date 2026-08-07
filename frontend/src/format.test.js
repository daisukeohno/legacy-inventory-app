import { describe, expect, it } from 'vitest';
import { formatYen } from './format.js';

describe('formatYen', () => {
  it('formats amounts with thousand separators and yen suffix', () => {
    expect(formatYen(128000)).toBe('128,000 円');
    expect(formatYen('261600')).toBe('261,600 円');
    expect(formatYen(0)).toBe('0 円');
  });

  it('returns an empty string for missing values', () => {
    expect(formatYen(null)).toBe('');
    expect(formatYen(undefined)).toBe('');
  });
});
