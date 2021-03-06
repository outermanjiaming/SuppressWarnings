package com.suppresswarnings.osgi.like;

import com.suppresswarnings.osgi.like.model.Page;
import com.suppresswarnings.osgi.like.model.Project;
import com.suppresswarnings.osgi.like.model.User;

public interface LikeHandler {

	public Page<Project> listProjects(boolean first, int n, String projectid, String openid);

	public String likeProject(String projectid, String openid);
	
	public String commentProject(String comment, String projectid, String openid, String commentid);

	public User myself(String openid);
}
