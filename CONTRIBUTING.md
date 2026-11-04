# Contributing to CityFix

Thank you for helping. Here are the steps.

1. Fork this repository on GitHub.
2. Clone your fork to your computer.
3. Create a branch for your work. Use a name like `fix/issue-12-duplicate-upvote`.
4. Pick an issue from the Issues tab. Write a comment on the issue so others know you are working on it.
5. Make your change. Keep it small and focused on the issue.
6. Add or update a test for your change.
7. Run `.\mvnw.cmd test` (Windows PowerShell) or `./mvnw test` (Mac and Linux) and make sure all tests pass.

   For browser-side JavaScript tests, run `node --test src/test/js/*.test.cjs` (Node.js built-in test runner; no extra packages required).
8. Push your branch and open a pull request. Write "Fixes #12" in the description (use the number of your issue).

If you are stuck, ask a question in a comment on the issue.
