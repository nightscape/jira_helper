JIRA Helper
===========
Helper for creating tasks and subtasks.

Usage
-----
First install/download `Ammonite` following the instructions from http://ammonite.io
Then create a YAML template like this
```yaml
subtasks:
  - summary: First subtask to be created
    description: Just do it
    status: Selected
  - summary: Second subtask to be created
```
and run
```
export JIRA_URL=http://mycompany.atlassian.net
export JIRA_USER=John
export JIRA_PASS=doe
amm -w JiraHelper.sc createSubtasksFromYaml --issueId PRJ-1234 --templateUrl http://some.server/issue_template.yaml
```
where `PRJ-1234` is an existing issue to which you want to add the subtasks from the template.
You can also use a file URL if you have the template stored locally.

