module.exports = {
  extends: ['@commitlint/config-conventional'],
  rules: {
    'type-enum': [
      2,
      'always',
      ['feat', 'fix', 'refactor', 'docs', 'style', 'test', 'chore', 'perf', 'build', 'ci']
    ],
    'subject-empty': [2, 'never'],
    'type-empty': [2, 'never']
  }
}
