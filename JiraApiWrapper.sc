import $file.Repos, Repos._
import $ivy.`com.atlassian.jira:jira-rest-java-client-core:4.0.0`
import $ivy.`com.atlassian.fugue:fugue:2.2.1`

import com.atlassian.jira.rest.client.api.GetCreateIssueMetadataOptionsBuilder
import com.atlassian.jira.rest.client.api.JiraRestClient
import com.atlassian.jira.rest.client.api.domain.BasicUser
import com.atlassian.jira.rest.client.api.domain.CimProject
import com.atlassian.jira.rest.client.api.domain.Issue
import com.atlassian.jira.rest.client.api.domain.IssueFieldId
import com.atlassian.jira.rest.client.api.domain.User
import com.atlassian.jira.rest.client.api.domain.Status
import com.atlassian.jira.rest.client.api.domain.Transition
import com.atlassian.jira.rest.client.api.domain.input.ComplexIssueInputFieldValue
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder
import com.atlassian.jira.rest.client.api.domain.input.FieldInput
import com.atlassian.jira.rest.client.api.domain.input.TransitionInput
import com.atlassian.jira.rest.client.auth.BasicHttpAuthenticationHandler
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory
import java.net.URI
import scala.collection.JavaConverters._

implicit class RichJiraClient(client: JiraRestClient) {
  def metadataProjects(projectId: String) = client.getIssueClient.getCreateIssueMetadata(new GetCreateIssueMetadataOptionsBuilder().withProjectKeys(projectId).withExpandedIssueTypesFields().build()).claim()
  def project(projectId: String) = metadataProjects(projectId).iterator.next
  def issue(issueId: String) = client.getIssueClient.getIssue(issueId).claim
  def findStatus(statusSubstring: String): Option[Status] = client.getMetadataClient.getStatuses.claim.asScala.find(_.getName.toLowerCase.contains(statusSubstring))
}

implicit class RichProject(project: CimProject)(implicit client: JiraRestClient) {
  def issueTypes = project.getIssueTypes.asScala.toArray
  def subtaskType = issueTypes.find(_.isSubtask).get
  def issue(issueId: String) = client.issue(issueId)
}

implicit class RichTransitions(transitions: Iterable[Transition]) {
  def findByName(name: String): Option[Transition] = transitions.find(_.getName.contains(name))
}

implicit class RichIssue(issue: Issue)(implicit client: JiraRestClient) {
  def project = client.project(issue.getProject.getKey)
  def transitions = client.getIssueClient.getTransitions(issue.getTransitionsUri).claim.asScala
  def findSubtask(summary: String): Option[Issue] =
    client
      .getSearchClient
      .searchJql(s"""parent = "${issue.getKey}" AND summary~"$summary"""")
      .claim
      .getIssues
      .asScala
      .headOption

  def createSubtask(summary: String, description: String = "",  status: Option[Status] = None) = {
    val issueInputBuilder =
      new IssueInputBuilder(project, project.subtaskType, summary)
        .setDescription(description)
        .setFieldValue("parent", ComplexIssueInputFieldValue.`with`("key", issue.getKey))

    val subtask = client.getIssueClient.createIssue(issueInputBuilder.build()).claim()
    val subtaskIssue = client.issue(subtask.getKey)
    status.foreach {desiredStatus =>
      val desiredTransition = subtaskIssue.transitions.findByName(desiredStatus.getName).get
			client
        .getIssueClient()
        .transition(subtaskIssue.getTransitionsUri(), new TransitionInput(desiredTransition.getId()))
				.claim()
    }
    client.issue(subtask.getKey)
  }
}

def createJiraClient(url: String, userName: String, password: String): JiraRestClient = {
  val clientFactory = new AsynchronousJiraRestClientFactory()
  val jiraServerUri: URI = new URI(url)
  clientFactory.create(jiraServerUri, new BasicHttpAuthenticationHandler(userName, password))
}
