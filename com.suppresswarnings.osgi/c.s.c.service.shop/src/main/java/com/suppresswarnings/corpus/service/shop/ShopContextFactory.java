/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.corpus.service.shop;

import java.util.concurrent.TimeUnit;

import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.service.AbstractAuthContextFactory;
import com.suppresswarnings.corpus.service.CorpusService;

public class ShopContextFactory extends AbstractAuthContextFactory {

	@Override
	public String command() {
		return ShopCtx.CMD;
	}

	@Override
	public String description() {
		return "用户进入我的商铺";
	}

	@Override
	public long ttl() {
		return TimeUnit.MINUTES.toMillis(15);
	}

	@Override
	public String[] requiredAuth() {
		return ShopCtx.AUTH;
	}

	@Override
	public Context<CorpusService> getContext(String wxid, String openid, CorpusService content) {
		return new ShopCtx(wxid, openid, content);
	}

}
