# Publishing the Python package to PyPI

The Python project metadata and publishing workflow are in this repository. The workflow uses PyPI Trusted Publishing (GitHub Actions OIDC) instead of a long-lived PyPI API token. PyPI must trust the repository/workflow before the upload will be accepted.

## One-time account setup

1. Sign in to the PyPI account that will own the package.
2. Open the account's Publishing / Trusted Publishers settings and add a publisher for a new project named `sms-forwarder-rules` (a pending publisher can be used for a project that has not yet been created).
3. Enter the exact values:
   - Owner: `sdwqe34`
   - Repository: `SMS_Forwarder`
   - Workflow filename: `.github/workflows/python-publish.yml`
   - Environment: `pypi`
4. Save the publisher. Confirm the project name is still available when PyPI accepts the first upload.

PyPI's official instructions:
- [Trusted Publishers overview](https://docs.pypi.org/trusted-publishers/)
- [Create a project through Trusted Publishing](https://docs.pypi.org/trusted-publishers/creating-a-project-through-oidc/)

## Publish the first release

1. Open the GitHub repository and go to **Actions → Publish Python package to PyPI**.
2. Select **Run workflow** on the default branch.
3. Wait for the tests/build job and then the publish job to complete successfully.
4. Verify the resulting project page: https://pypi.org/project/sms-forwarder-rules/

The package version is read from `python/pyproject.toml` (currently `0.1.0`). PyPI does not allow overwriting an already-published version. Before each future release, bump the version in `pyproject.toml` and `python/sms_forwarder/__init__.py` together.

## Security and capabilities

No PyPI API token is stored in this repository. The publishing job requests an OIDC identity token and receives a short-lived credential from PyPI after trust is verified.

The package is a rules and destination-planning library only. It doesn't access an Android inbox, send SMS, log into Telegram/WhatsApp/Bale/Eitaa, or perform actual delivery.
