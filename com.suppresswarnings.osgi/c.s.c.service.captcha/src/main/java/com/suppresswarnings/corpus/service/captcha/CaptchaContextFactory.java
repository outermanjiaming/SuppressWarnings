package com.suppresswarnings.corpus.service.captcha;

import java.util.concurrent.TimeUnit;

import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.common.ContextFactory;
import com.suppresswarnings.corpus.service.CorpusService;

public class CaptchaContextFactory implements ContextFactory<CorpusService> {

	@Override
	public String command() {
		return CaptchaContext.CMD;
	}

	@Override
	public String description() {
		return "素朴网联提供临时支付通道";
	}

	@Override
	public long ttl() {
		return TimeUnit.MINUTES.toMillis(3);
	}

	@Override
	public Context<CorpusService> getInstance(String wxid, String openid, CorpusService content) {
		return new CaptchaContext(wxid, openid, content);
	}
	
}
