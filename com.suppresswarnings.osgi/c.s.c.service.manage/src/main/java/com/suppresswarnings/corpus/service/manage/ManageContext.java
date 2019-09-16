/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.corpus.service.manage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

import com.suppresswarnings.corpus.common.Const;
import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.common.KeyValue;
import com.suppresswarnings.corpus.common.State;
import com.suppresswarnings.corpus.service.CorpusService;
import com.suppresswarnings.corpus.service.WXContext;
import com.suppresswarnings.corpus.service.daigou.Goods;
import com.suppresswarnings.corpus.service.wx.WXuser;

public class ManageContext extends WXContext {
	public static final String CMD = "我的后台管理";
	public static final String[] AUTH = {"Manage"};
	
	State<Context<CorpusService>> enter = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = -7540314541740975610L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			u.output("http://suppresswarnings.com/managereports.html");
			u.output("后台管理，等级森严，你可以输入以下指令：");
			u.output(goodsManage.name());
			u.output(examManage.name());
			u.output(cashoutManage.name());
			u.output(alert.name());
			u.output(rawSetting.name());
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			if(goodsManage.name().equals(t)) return goodsManage;
			if(examManage.name().equals(t)) return examManage;
			if(cashoutManage.name().equals(t)) return cashoutManage;
			if(alert.name().equals(t)) return alert;
			if(rawSetting.name().equals(t)) return rawSetting;
			return enter;
		}

		@Override
		public String name() {
			return "后台管理入口";
		}

		@Override
		public boolean finish() {
			return false;
		}
	};
	State<Context<CorpusService>> rawSetting = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1490645437562410681L;
		State<Context<CorpusService>> put = new State<Context<CorpusService>>() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 842887322287890300L;

			@Override
			public void accept(String t, Context<CorpusService> u) {
				String[] kv = t.split("/", 2);
				String key = kv[0];
				String value = kv[1];
				String old = u.content().account().get(key);
				u.content().account().put(key, value);
				u.content().account().put(String.join(Const.delimiter, Const.Version.V1, "Info", "RawSetting", key), key);
				u.output(String.format("设置成功：\nkey:%s\nval:%s\nold:%s", key, value, old));
			}

			@Override
			public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
				return rawSetting;
			}

			@Override
			public String name() {
				return "设置值";
			}

			@Override
			public boolean finish() {
				return true;
			}
			
		};
		@Override
		public void accept(String t, Context<CorpusService> u) {
			String head = String.join(Const.delimiter, Const.Version.V1, "Info", "RawSetting");
			u.output("以前配置过的key：");
			u.content().account().page(head, head, null, 1000, new BiConsumer<String, String>() {
				
				@Override
				public void accept(String k, String v) {
					u.output(v);
				}
			});
			u.output("请按格式输入：key/value");
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			return put;
		}

		@Override
		public String name() {
			return "绑定配置";
		}

		@Override
		public boolean finish() {
			return false;
		}
		
	};
	
	State<Context<CorpusService>> cashoutManage = new State<Context<CorpusService>>() {
		/**
		 * 
		 */
		private static final long serialVersionUID = -5073508375477564133L;
		Iterator<String> itr;
		@Override
		public void accept(String t, Context<CorpusService> u) {
			boolean admin = u.content().isAdmin(openid(), "cashoutManage", time());
			if(admin) {
				u.output("欢迎使用工资管理");
				Set<String> set = u.content().todoSet();
				if(set.size() > 0) {
					u.output("待审核的提现请求：" + set.size());
					itr = set.iterator();
					u.output("请输入：" + approve.name());
				}
				u.output("请输入：" + salary.name());
			} else {
				u.output("目前仅支持管理员发工资");
			}
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			if(approve.name().equals(t)) return approve;
			if(salary.name().equals(t)) return salary;
			return enter;
		}

		@Override
		public String name() {
			return "工资管理";
		}

		@Override
		public boolean finish() {
			return false;
		}
		
		State<Context<CorpusService>> salary = new State<Context<CorpusService>>() {
			WXuser wxuser = null;
			int totalcent = 0;
			
			/**
			 * 
			 */
			private static final long serialVersionUID = 7387172215455497024L;

			@Override
			public void accept(String t, Context<CorpusService> u) {
				u.output("请输入员工的ID：");
			}

			@Override
			public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
				return crew;
			}

			@Override
			public String name() {
				return "发工资";
			}

			@Override
			public boolean finish() {
				return false;
			}
			State<Context<CorpusService>> confirm = new State<Context<CorpusService>>() {

				/**
				 * 
				 */
				private static final long serialVersionUID = 2406201958772687441L;

				@Override
				public void accept(String t, Context<CorpusService> u) {
					if(name().equals(t)) {
						u.output(String.format("系统已经接受请求，正在发工资给(%s)%s，总共%d分", wxuser.getOpenid(), wxuser.getNickname(), totalcent));
						String ret = u.content().reward("素朴网联发工资", wxuser.getOpenid(), totalcent);
						u.output("工资发放结果：" + ret);
						totalcent = 0;
						wxuser = null;
					} else {
						u.output(String.format("你正在发工资给(%s)%s，总共%d分，请输入：确认", wxuser.getOpenid(), wxuser.getNickname(), totalcent));
					}
				}

				@Override
				public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
					if("取消".equals(t) || wxuser == null) return cashoutManage;
					return confirm;
				}

				@Override
				public String name() {
					return "确认";
				}

				@Override
				public boolean finish() {
					return true;
				}
				
			};
			State<Context<CorpusService>> money = new State<Context<CorpusService>>() {
				/**
				 * 
				 */
				private static final long serialVersionUID = 5009566210156032984L;
				boolean fail = true;
				@Override
				public void accept(String t, Context<CorpusService> u) {
					String amount = t;
					try {
						int cent = Integer.parseInt(amount);
						if(cent < 30) {
							u.output("工资金额最低30分，请重新输入：（单位：分）");
							fail = true;
						} else if (cent > 10000) {
							u.output("目前最大金额限制为10000分，请重新输入：（单位：分）");
							fail = true;
						} else  {
							fail = false;
							totalcent = cent;
							u.output(String.format("你正在发工资给(%s)%s，总共%d分，(如果想放弃，请输入：取消)请输入：确认", wxuser.getOpenid(), wxuser.getNickname(), cent));
						}
					} catch (Exception e) {
						logger.error("工资金额不对", e);
						fail = true;
					}
				}

				@Override
				public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
					if(fail) {
						fail = true;
						return money;
					}
					return confirm;
				}

				@Override
				public String name() {
					return "工资金额";
				}

				@Override
				public boolean finish() {
					return false;
				}
				
			};
			State<Context<CorpusService>> crew = new State<Context<CorpusService>>() {

				/**
				 * 
				 */
				private static final long serialVersionUID = 7472872732591041261L;

				@Override
				public void accept(String t, Context<CorpusService> u) {
					String userid = t;
					wxuser = u.content().getWXuserByOpenId(userid);
					if(wxuser == null) {
						u.output("请输入正确的员工ID：");
					} else {
						u.output(String.format("你将发工资给(%s)%s，请输入工资金额：（单位：分，比如输入30就是30分，也就是0.3元）", wxuser.getOpenid(), wxuser.getNickname()));
					}
				}

				@Override
				public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
					if(wxuser != null) {
						return money;
					}
					return crew;
				}

				@Override
				public String name() {
					return "员工openid";
				}

				@Override
				public boolean finish() {
					return false;
				}
				
			};
			
		};
		
		State<Context<CorpusService>> approve = new State<Context<CorpusService>>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = -2938299575404413791L;
			String string;
			WXuser who;
			KeyValue kv;
			boolean finish = false;
			@Override
			public void accept(String t, Context<CorpusService> u) {
				if(name().equals(t)) {
					u.output("你正在审核提现请求，请认真操作，每一步操作都将详细追踪，退出请输入：完成");
				} else {
					if(string == null) {
						u.output("用户openid不存在");
					} else {
						if(who == null || who.getSubscribe() == 0) {
							u.output("用户不存在");
						} else {
							if("同意".equals(t)) {
								u.output("你同意了审核提现请求: " + string + " "  + who.getNickname());
								int cent = Integer.parseInt(kv.value());
								u.output("正在执行: " + cent);
								String result = u.content().reward("素朴网联同意提现", string, cent);
								u.output(result);
								u.content().account().put(String.join(Const.delimiter, Const.Version.V1, "Info", "Accept", "Cashout", openid(), time()), kv.toString());
								u.content().atUser(string, "恭喜你，素朴网联 同意了你的提现请求：" + kv.value() + "分，请查看微信零钱。");
							} else if("拒绝".equals(t)){
								u.output("你拒绝了审核提现请求: "  + string + " " + who.getNickname());
								u.content().rejectRunnable(openid(), string, Integer.parseInt(kv.value()));
								u.content().account().put(String.join(Const.delimiter, Const.Version.V1, "Info", "Reject", "Cashout", openid(), time()), kv.toString());
								u.content().atUser(string, "非常抱歉，素朴网联 拒绝了你的提现请求：" + kv.value() + "分，不要气馁，还有机会。");
							} else {
								u.content().account().put(String.join(Const.delimiter, Const.Version.V1, "Info", "Wrong", "Cashout", openid(), time()), kv.toString());
								u.output("你输入了错误的命令，该用户的提现请求无法被再次审核：" + string + " " + who.getNickname());
							}
						}
					}
				}
				if(itr != null && itr.hasNext()) {
					string = itr.next();
					who = u.content().getWXuserByOpenId(string);
					if(who == null || who.getSubscribe() == 0) {
						u.output("用户不存在");
					} else {
						kv = u.content().approve(string);
						u.output("你正在审核" + string + " " + who.getNickname() + "提现请求：" + kv.value() + "分");
					}
					u.output("请输入「同意」或「拒绝」");
				} else {
					finish = true;
					u.output("你完成了所有的提现申请");
				}
			}

			@Override
			public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
				if("完成".equals(t) || finish()) {
					finish = false;
					return enter;
				}
				
				return approve;
			}

			@Override
			public String name() {
				return "审核";
			}

			@Override
			public boolean finish() {
				return finish;
			}
			
		};
	};
	
	State<Context<CorpusService>> examManage = new State<Context<CorpusService>>() {


		/**
		 * 
		 */
		private static final long serialVersionUID = 1781563629242769303L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			u.output("你可以输入");
			u.output("    关闭测试");
			u.output("    打开测试");
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			if("关闭测试".equals(t)) return examOff;
			if("打开测试".equals(t)) return examOn;
			return examManage;
		}

		@Override
		public String name() {
			return "测试管理";
		}

		@Override
		public boolean finish() {
			return false;
		}
	};
	State<Context<CorpusService>> examOff = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = 248223907177679117L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			String onKey = String.join(Const.delimiter, Const.Version.V2, "Collect", "Corpus", "ON");
			String on = u.content().data().get(onKey);
			if("off".equals(on)) {
				u.output("已经关闭");
			}else {
				u.content().data().put(onKey, "off");
				u.output("关闭测试了");
			}
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			return examManage;
		}

		@Override
		public String name() {
			return "关闭测试";
		}

		@Override
		public boolean finish() {
			return false;
		}
	};
	State<Context<CorpusService>> examOn = new State<Context<CorpusService>>() {


		/**
		 * 
		 */
		private static final long serialVersionUID = -3071885384883325588L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			String onKey = String.join(Const.delimiter, Const.Version.V2, "Collect", "Corpus", "ON");
			String on = u.content().data().get(onKey);
			if("on".equals(on)) {
				u.output("已经打开");
			} else {
				u.content().data().put(onKey, "on");
				u.output("打开测试了");
			}
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			return examManage;
		}

		@Override
		public String name() {
			return "关闭测试";
		}

		@Override
		public boolean finish() {
			return false;
		}
	};
	
	State<Context<CorpusService>> goodsManage = new State<Context<CorpusService>>() {
		Goods todo = null;
		AtomicInteger mode = new AtomicInteger(0);
		public String display(Goods todo) {
			StringBuffer sb = new StringBuffer(todo.getState());
			sb.append(todo.getTitle());
			sb.append("\n").append("¥：" + todo.getPricecent() + "," + todo.getPriceagent() + "," + todo.getPricevip() + "," + todo.getPricesecret());
			return sb.toString();
		}
		
		State<Context<CorpusService>> goodsCreate = new State<Context<CorpusService>>() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 6223810646770795371L;

			@Override
			public void accept(String t, Context<CorpusService> u) {
				u.output("输入商品标题（一句话，一目了然）");
				if(todo == null) {
					todo = new Goods();
					String goodsid = time() + "_" + random();
					todo.setGoodsid(goodsid);
				}
				todo.setTime(time());
				if(mode.get() == 1) {
					u.output("旧：" + todo.getTitle());
				}
			}

			@Override
			public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
				if("完成修改".equals(t)) return goodsFinish;
				return goodsName;
			}

			@Override
			public String name() {
				return "创建商品";
			}

			@Override
			public boolean finish() {
				return false;
			}
			
		};
		State<Context<CorpusService>> goodsName = new State<Context<CorpusService>>() {

			/**
			 * 
			 */
			private static final long serialVersionUID = -8880661297292678751L;

			@Override
			public void accept(String t, Context<CorpusService> u) {
				if(!"不修改".equals(t)) todo.setTitle(t);
				u.output("输入商品外部链接地址");
				if(mode.get() == 1) {
					u.output("旧：" + todo.getOuturl());
				}
			}

			@Override
			public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
				if("完成修改".equals(t)) return goodsFinish;
				return goodsOuturl;
			}

			@Override
			public String name() {
				return "商品名称";
			}

			@Override
			public boolean finish() {
				return false;
			}
			
		};
		
		State<Context<CorpusService>> goodsOuturl = new State<Context<CorpusService>>() {

			/**
			 * 
			 */
			private static final long serialVersionUID = -8996015865773382247L;

			@Override
			public void accept(String t, Context<CorpusService> u) {
				if(!"不修改".equals(t)) todo.setOuturl(t);
				u.output("输入商品特色（比如：新西兰仓库，澳洲直邮）");
				if(mode.get() == 1) {
					u.output("旧：" + todo.getExtra());
				}
			}

			@Override
			public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
				if("完成修改".equals(t)) return goodsFinish;
				return goodsExtra;
			}

			@Override
			public String name() {
				return "商品特色";
			}

			@Override
			public boolean finish() {
				return false;
			}
			
		};
		
		State<Context<CorpusService>> goodsExtra = new State<Context<CorpusService>>() {

			/**
			 * 
			 */
			private static final long serialVersionUID = -3792470520505502634L;

			@Override
			public void accept(String t, Context<CorpusService> u) {
				if(!"不修改".equals(t)) todo.setExtra(t);
				u.output("输入商品库存（数字）");
				if(mode.get() == 1) {
					u.output("旧：" + todo.getQuota());
				}
			}

			@Override
			public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
				if("完成修改".equals(t)) return goodsFinish;
				return goodsQuota;
			}

			@Override
			public String name() {
				return "商品特色";
			}

			@Override
			public boolean finish() {
				return false;
			}
			
		};
		
		State<Context<CorpusService>> goodsQuota = new State<Context<CorpusService>>() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 6209214087731374248L;

			@Override
			public void accept(String t, Context<CorpusService> u) {
				if(!"不修改".equals(t)) todo.setQuota(t);
				u.output("输入商品价格（单位：分）");
				if(mode.get() == 1) {
					u.output("旧：" + todo.getPricecent());
				}
			}

			@Override
			public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
				if("完成修改".equals(t)) return goodsFinish;
				return goodsPrice;
			}

			@Override
			public String name() {
				return "商品库存";
			}

			@Override
			public boolean finish() {
				return false;
			}
			
		};
		
		State<Context<CorpusService>> goodsPriceagent = new State<Context<CorpusService>>() {

			/**
			 * 
			 */
			private static final long serialVersionUID = -1626839693582430055L;

			@Override
			public void accept(String t, Context<CorpusService> u) {
				if(!"不修改".equals(t)) todo.setPriceagent(t);
				u.output("输入商品VIP价格（单位：分）");
				if(mode.get() == 1) {
					u.output("旧：" + todo.getPricevip());
				}
			}

			@Override
			public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
				if("完成修改".equals(t)) return goodsFinish;
				return goodsPricevip;
			}

			@Override
			public String name() {
				return "商品价钱Agent";
			}

			@Override
			public boolean finish() {
				return false;
			}
			
		};
		
		State<Context<CorpusService>> goodsPricevip = new State<Context<CorpusService>>() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 2597039011439778871L;

			@Override
			public void accept(String t, Context<CorpusService> u) {
				if(!"不修改".equals(t)) todo.setPricevip(t);
				u.output("输入商品Secret价格（单位：分）");
				if(mode.get() == 1) {
					u.output("旧：" + todo.getPricesecret());
				}
			}

			@Override
			public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
				if("完成修改".equals(t)) return goodsFinish;
				return goodsPricesecret;
			}

			@Override
			public String name() {
				return "商品价钱VIP";
			}

			@Override
			public boolean finish() {
				return false;
			}
			
		};
		
		State<Context<CorpusService>> goodsPricesecret = new State<Context<CorpusService>>() {

			/**
			 * 
			 */
			private static final long serialVersionUID = -6991986613864852205L;

			@Override
			public void accept(String t, Context<CorpusService> u) {
				if(!"不修改".equals(t)) todo.setPricesecret(t);
				u.output("请上传一张商品图片");
				if(mode.get() == 1) {
					u.output("旧：" + todo.getImage());
				}
			}

			@Override
			public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
				if("完成修改".equals(t)) return goodsFinish;
				return goodsImage;
			}

			@Override
			public String name() {
				return "商品价钱Secret";
			}

			@Override
			public boolean finish() {
				return false;
			}
			
		};
		
		
		State<Context<CorpusService>> goodsPrice = new State<Context<CorpusService>>() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 5695039305616072090L;

			@Override
			public void accept(String t, Context<CorpusService> u) {
				if(!"不修改".equals(t)) todo.setPricecent(t);
				u.output("输入商品Agent价格（单位：分）");
				if(mode.get() == 1) {
					u.output("旧：" + todo.getPriceagent());
				}
			}

			@Override
			public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
				if("完成修改".equals(t)) return goodsFinish;
				return goodsPriceagent;
			}

			@Override
			public String name() {
				return "商品图片";
			}

			@Override
			public boolean finish() {
				return false;
			}
			
		};
		
		State<Context<CorpusService>> goodsImage = new State<Context<CorpusService>>() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 24808056831639001L;

			@Override
			public void accept(String t, Context<CorpusService> u) {
				String image = t;
				if(t.startsWith("IMAGE_")) {
					image = t.substring("IMAGE_".length());
				}
				if(!"不修改".equals(t)) todo.setImage(image);
				u.output("输入商品详细图片说明（连续上传图片）");
				if(mode.get() == 1) {
					u.output("旧：" + todo.getListimages());
				}
			}

			@Override
			public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
				if("完成修改".equals(t)) return goodsFinish;
				return goodsImagelist;
			}

			@Override
			public String name() {
				return "商品图片";
			}

			@Override
			public boolean finish() {
				return false;
			}
			
		};
		
		State<Context<CorpusService>> goodsImagelist = new State<Context<CorpusService>>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = -5184762709328382480L;
			List<String> images = new ArrayList<>();
			@Override
			public void accept(String t, Context<CorpusService> u) {
				if("不修改".equals(t)) {
					u.output("详细描述图不修改，请输入'不修改'则结束");
					return;
				}
				String image = t;
				if(t.startsWith("IMAGE_")) {
					image = t.substring("IMAGE_".length());
				}
				images.add(image);
				u.output("请继续上传，输入'完成修改'则结束");
			}

			@Override
			public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
				if("不修改".equals(t)) {
					return goodsFinish;
				}
				
				if("完成修改".equals(t)) {
					todo.setListimages(String.join(",", images));
					images.clear();
					return goodsFinish;
				}
				
				return goodsImagelist;
			}

			@Override
			public String name() {
				return "商品图片描述";
			}

			@Override
			public boolean finish() {
				return false;
			}
			
		};
		State<Context<CorpusService>> goodsFinish = new State<Context<CorpusService>>() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 520903050877528334L;

			@Override
			public void accept(String t, Context<CorpusService> u) {
				u.output("商品详情");
				u.output(display(todo));
				u.output("请输入'确认'则保存至数据库");
			}

			@Override
			public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
				if("确认".equals(t)) return goodsConfirm;
				return goodsFinish;
			}

			@Override
			public String name() {
				return "查看商品数据";
			}

			@Override
			public boolean finish() {
				return false;
			}
			
		};
		State<Context<CorpusService>> goodsConfirm = new State<Context<CorpusService>>() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 2653687424983970872L;

			@Override
			public void accept(String t, Context<CorpusService> u) {
				u.content().daigouHandler.saveMyGoods(openid(), todo);
				todo = null;
				mode.set(0);
				u.output("已保存商品数据");
			}

			@Override
			public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
				return goodsManage;
			}

			@Override
			public String name() {
				return "保存商品数据";
			}

			@Override
			public boolean finish() {
				return false;
			}
			
		};
		
		State<Context<CorpusService>> goodsOff = new State<Context<CorpusService>>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 3405658341549718899L;

			State<Context<CorpusService>> goodsLack = new State<Context<CorpusService>>() {

				/**
				 * 
				 */
				private static final long serialVersionUID = -2043730208805465523L;

				@Override
				public void accept(String t, Context<CorpusService> u) {
					String goodsid = todo.getGoodsid();
					String keyState = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Detail", "Goods", goodsid, "State");
					u.content().account().put(keyState, "LACK");
					u.output("商品状态已经设置为LACK，暂时缺货");
				}

				@Override
				public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
					return goodsManage;
				}

				@Override
				public String name() {
					return "缺货";
				}

				@Override
				public boolean finish() {
					return false;
				}
				
			};
			
			State<Context<CorpusService>> goodsDown = new State<Context<CorpusService>>() {

				/**
				 * 
				 */
				private static final long serialVersionUID = -5663807013247315793L;

				@Override
				public void accept(String t, Context<CorpusService> u) {
					String goodsid = todo.getGoodsid();
					String keyState = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Detail", "Goods", goodsid, "State");
					u.content().account().put(keyState, "DOWN");
					u.output("商品状态已经设置为DOWN，已下架");
				}

				@Override
				public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
					return goodsManage;
				}

				@Override
				public String name() {
					return "下架";
				}

				@Override
				public boolean finish() {
					return false;
				}
				
			};
			
			State<Context<CorpusService>> goodsDelete = new State<Context<CorpusService>>() {

				/**
				 * 
				 */
				private static final long serialVersionUID = 7168011886175885716L;

				@Override
				public void accept(String t, Context<CorpusService> u) {
					String goodsid = todo.getGoodsid();
					String keyState = String.join(Const.delimiter, Const.Version.V1, "Daigou", "Detail", "Goods", goodsid, "State");
					u.content().account().put(keyState, "DELETE");
					u.output("商品状态已经设置为DELETE，已删除");
				}

				@Override
				public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
					return goodsManage;
				}

				@Override
				public String name() {
					return "缺货";
				}

				@Override
				public boolean finish() {
					return false;
				}
				
			};
			@Override
			public void accept(String t, Context<CorpusService> u) {
				u.output("下架-商品详情");
				u.output(display(todo));
				u.output("请输入下架理由：缺货、下架、删除\n如果不下架，则输入'不下架'");
			}

			@Override
			public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
				if("缺货".equals(t)) return goodsLack;
				if("下架".equals(t)) return goodsDown;
				if("删除".equals(t)) return goodsDelete;
				
				
				todo = null;
				return goodsManage;
			}

			@Override
			public String name() {
				return "下架商品";
			}

			@Override
			public boolean finish() {
				return false;
			}
			
		};
		
		State<Context<CorpusService>> goodsModify = new State<Context<CorpusService>>() {

			/**
			 * 
			 */
			private static final long serialVersionUID = -3519420607410387813L;

			@Override
			public void accept(String t, Context<CorpusService> u) {
				u.output("商品详情");
				u.output(display(todo));
				u.output("请输入'确认'进行修改产品信息\n根据提示直接输入新内容\n如果不需要修改，就输入'不修改'\n输入'完成修改'则直接完成");
			}

			@Override
			public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
				if("确认".equals(t)) {
					mode.set(1);
					return goodsCreate;
				}
				mode.set(0);
				todo = null;
				return goodsManage;
			}

			@Override
			public String name() {
				return "修改商品数据";
			}

			@Override
			public boolean finish() {
				return false;
			}
			
		};
		
		Map<String, Goods> goodsMap;

		/**
		 * 
		 */
		private static final long serialVersionUID = -3941602152166765573L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			if(goodsMap == null) {
				goodsMap = new HashMap<>();
				String mygoodsidKey = String.join(Const.delimiter, Const.Version.V1, "Daigou", openid(), "Goodsid");
				AtomicInteger integer = new AtomicInteger(0);
				u.content().account().page(mygoodsidKey, mygoodsidKey, null, 1000, (x, key) -> {
					Goods goods = u.content().daigouHandler.getByGoodsid(key);
					if(goods != null) {
						u.content().daigouHandler.fillGoodsWithAllPrice(goods, openid(), openid());
						u.content().daigouHandler.fillOuturl(goods);
						goodsMap.put("" + integer.getAndIncrement(), goods);
					}
				});
			}
			if(goodsMap.isEmpty()) {
				u.output("你没有商品。你可以输入");
				u.output("    创建商品");
			} else {
				u.output("你可以输入");
				u.output("    创建商品");
				u.output("    修改商品+id");
				u.output("    下架商品+id");
				u.output("举例：修改商品1，下架商品3");
				goodsMap.forEach((id, goods) ->{
					u.output("id: " + id);
					u.output(display(goods));
					u.output(" - - - ");
				});
			}
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			if("创建商品".equals(t)){
				logger.info("[manage] goods create");
				todo = null;
				return goodsCreate;
			} else if(t.startsWith("修改商品")) {
				logger.info("[manage] goods modify");
				String index = t.substring("修改商品".length());
				todo = goodsMap.get(index);
				return goodsModify;
			} else if(t.startsWith("下架商品")) {
				logger.info("[manage] goods off");
				
				String index = t.substring("下架商品".length());
				todo = goodsMap.get(index);
				return goodsOff;
			}
			return half;
		}

		@Override
		public String name() {
			return "商品管理";
		}

		@Override
		public boolean finish() {
			return false;
		}
	};
	
	State<Context<CorpusService>> half = new State<Context<CorpusService>>() {


		/**
		 * 
		 */
		private static final long serialVersionUID = 3437670267016168193L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			u.output("哈哈哈，还没实现");
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			return enter;
		}

		@Override
		public String name() {
			return "暂未实现";
		}

		@Override
		public boolean finish() {
			return false;
		}
	};
	
	State<Context<CorpusService>> alert = new State<Context<CorpusService>>() {
		/**
		 * 
		 */
		private static final long serialVersionUID = -6589604222047894996L;
		State<Context<CorpusService>> info = new State<Context<CorpusService>>() {


			/**
			 * 
			 */
			private static final long serialVersionUID = -8524769684511160999L;

			@Override
			public void accept(String t, Context<CorpusService> u) {
				u.output("正在发送公告");
			}

			@Override
			public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
				
				return init;
			}

			@Override
			public String name() {
				return "发送公告";
			}

			@Override
			public boolean finish() {
				return false;
			}
		};
		State<Context<CorpusService>> alluser = new State<Context<CorpusService>>() {


			/**
			 * 
			 */
			private static final long serialVersionUID = -4362155309121757653L;

			@Override
			public void accept(String t, Context<CorpusService> u) {
				u.output("所有用户总共有N人，正在发送公告");
			}

			@Override
			public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
				
				return info;
			}

			@Override
			public String name() {
				return "所有人";
			}

			@Override
			public boolean finish() {
				return false;
			}
		};
		
		State<Context<CorpusService>> allvip = new State<Context<CorpusService>>() {


			/**
			 * 
			 */
			private static final long serialVersionUID = -3416552825809522341L;

			@Override
			public void accept(String t, Context<CorpusService> u) {
				u.output("所有vip总共有N人，正在发送公告");
			}

			@Override
			public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
				
				return info;
			}

			@Override
			public String name() {
				return "所有vip";
			}

			@Override
			public boolean finish() {
				return false;
			}
		};
		
		State<Context<CorpusService>> myvip = new State<Context<CorpusService>>() {


			/**
			 * 
			 */
			private static final long serialVersionUID = -750136022994626832L;

			@Override
			public void accept(String t, Context<CorpusService> u) {
				u.output("你授权开通的vip总共有N人，正在发送公告");
			}

			@Override
			public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
				
				return info;
			}

			@Override
			public String name() {
				return "我的vip";
			}

			@Override
			public boolean finish() {
				return false;
			}
		};
		
		State<Context<CorpusService>> mycrew = new State<Context<CorpusService>>() {


			/**
			 * 
			 */
			private static final long serialVersionUID = 7904523441105860956L;

			@Override
			public void accept(String t, Context<CorpusService> u) {
				u.output("你邀请的朋友总共有N人，正在发送公告");
			}

			@Override
			public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
				
				return info;
			}

			@Override
			public String name() {
				return "我的员工";
			}

			@Override
			public boolean finish() {
				return false;
			}
		};

		@Override
		public void accept(String t, Context<CorpusService> u) {
			u.output("步骤：1.选择人群，2.输入公告，3.确认发布公告");
			u.output("你要向哪些人发布公告？请输入：所有人，所有vip, 我的vip，我的员工");
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			if(alluser.name().equals(t)) return alluser;
			if(allvip.name().equals(t)) return allvip;
			if(myvip.name().equals(t)) return myvip;
			if(mycrew.name().equals(t)) return mycrew;
			return alert;
		}

		@Override
		public String name() {
			return "发布公告";
		}

		@Override
		public boolean finish() {
			return false;
		}
	};
	
	public ManageContext(String wxid, String openid, CorpusService ctx) {
		super(wxid, openid, ctx);
		this.state = enter;
	}

}
