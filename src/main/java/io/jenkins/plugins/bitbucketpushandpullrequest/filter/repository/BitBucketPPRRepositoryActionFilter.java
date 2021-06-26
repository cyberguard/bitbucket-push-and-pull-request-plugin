/*******************************************************************************
 * The MIT License
 * 
 * Copyright (C) 2020, CloudBees, Inc.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/


package io.jenkins.plugins.bitbucketpushandpullrequest.filter.repository;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import hudson.EnvVars;
import hudson.model.AbstractDescribableImpl;
import io.jenkins.plugins.bitbucketpushandpullrequest.action.BitBucketPPRAction;
import io.jenkins.plugins.bitbucketpushandpullrequest.cause.BitBucketPPRTriggerCause;
import io.jenkins.plugins.bitbucketpushandpullrequest.model.BitBucketPPRHookEvent;
import io.jenkins.plugins.bitbucketpushandpullrequest.util.BitBucketPPRUtils;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundSetter;
import java.util.logging.Logger;


public abstract class BitBucketPPRRepositoryActionFilter
    extends AbstractDescribableImpl<BitBucketPPRRepositoryActionFilter> {
  private static final Logger logger = Logger.getLogger(BitBucketPPRRepositoryActionFilter.class.getName());

  public abstract boolean shouldTriggerBuild(BitBucketPPRAction bitbucketAction);

  public abstract BitBucketPPRTriggerCause getCause(File pollingLog,
      BitBucketPPRAction bitbucketAction, BitBucketPPRHookEvent bitBucketEvent) throws IOException;

  public boolean matches(String allowedBranches, String branchName, EnvVars env) {
    return BitBucketPPRUtils.matches(allowedBranches, branchName, env);
  }

  public abstract boolean shouldTriggerAlsoIfNothingChanged();

  public abstract boolean shouldSendApprove();

  public Map<String, String> prRepoOverrides = new HashMap<String,String>();

  @DataBoundSetter
  public void setPrRepoOverrides(String prRepoOverrides){
    logger.info( prRepoOverrides );
    if ( prRepoOverrides != null ){
      JSONObject cfg = JSONObject.fromObject( prRepoOverrides);
      for (Iterator<String> itt = cfg.keys(); itt.hasNext(); ){
        String orig = itt.next();
        String override = cfg.getString( orig );
        this.prRepoOverrides.put( orig, override);
      }
    }
  }

  public String getPrRepoOverrides(){
    JSONObject cfg = JSONObject.fromObject( prRepoOverrides);
    return cfg.toString( );
  }

  public String getRepositoryOverrideForUrl(String orig_repo_url ){
    for (Map.Entry<String, String> entry : prRepoOverrides.entrySet()) {
      String bitBucketRepositoryUrl = entry.getKey();
      String gitScmRepositoryUrl = entry.getValue();
      if (orig_repo_url.equals( bitBucketRepositoryUrl ) ){
        logger.finest("Git URL override for " + bitBucketRepositoryUrl + " is " + gitScmRepositoryUrl);
        return gitScmRepositoryUrl ;
      }
    }
    return orig_repo_url;
  }
}
