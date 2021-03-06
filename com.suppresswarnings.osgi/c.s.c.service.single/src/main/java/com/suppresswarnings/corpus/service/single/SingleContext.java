package com.suppresswarnings.corpus.service.single;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.suppresswarnings.corpus.common.Const;
import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.common.KeyValue;
import com.suppresswarnings.corpus.common.State;
import com.suppresswarnings.corpus.service.CorpusService;
import com.suppresswarnings.corpus.service.WXContext;

public class SingleContext extends WXContext {
	public static final String CMD = "我是单身";
	
	State<Context<CorpusService>> single = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = -9036545818401656956L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			u.content().account().put(String.join(Const.delimiter, Const.Version.V1, openid(), "Tag", "Single"), openid());
			u.content().account().put(String.join(Const.delimiter, Const.Version.V1, "Tag", "Single", openid()), openid());
			AtomicInteger count = new AtomicInteger(0);
			String start = String.join(Const.delimiter, Const.Version.V1, "Tag", "Single");
			u.content().account().page(start, start, null, Integer.MAX_VALUE, (k,v) ->{
				count.incrementAndGet();
			});
			count.addAndGet(u.content().users.size());
			u.content().data().put(String.join(Const.delimiter, Const.Version.V1, "Tag", "Single", time(), random()),  openid());
			
			u.output("「素朴网联」已经拥有了"+count.get()+"位单身，你需要找男朋友还是女朋友？请输入：\n我要男朋友\n我要女朋友");
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			if(CMD.equals(t)) {
				return single;
			}
			if(girlfriend.name().equals(t)) {
				return girlfriend;
			}
			if(boyfriend.name().equals(t)) {
				return boyfriend;
			}
			u.output("你输入的对吗？请阅读上面那条信息。");
			return init;
		}

		@Override
		public String name() {
			return CMD;
		}

		@Override
		public boolean finish() {
			return false;
		}
	};
	
	
	State<Context<CorpusService>> question = new State<Context<CorpusService>>() {
		
		State<Context<CorpusService>> finished = new State<Context<CorpusService>>() {

			/**
			 * 
			 */
			private static final long serialVersionUID = -8031796296634830830L;

			@Override
			public void accept(String t, Context<CorpusService> u) {
				u.content().account().put(String.join(Const.delimiter, Const.Version.V1, "Info", "Self", "Introduce", openid()),  t);
				u.content().account().put(String.join(Const.delimiter, Const.Version.V1, openid(), "Self", "Introduce"),  t);
				u.content().data().put(String.join(Const.delimiter, Const.Version.V1, "Info", "Self", "Introduce", openid(), time()),  t);
				u.output("太好了，未来对象正在寻找你！你可以多提一些问题，或者多回答一些问题。");
			}

			@Override
			public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
				return init;
			}

			@Override
			public String name() {
				return "完成";
			}

			@Override
			public boolean finish() {
				return true;
			}
			
		};
		
		List<String> quiz = new ArrayList<>();
		boolean finish = false;
		
		String[] quizs = {
				"你最想问什么？",
				"你还想问什么？",
				"再问一个更尖锐的问题？",
				"最后问一个无关紧要的问题？"
		};
		int index = 0;
		/**
		 * 
		 */
		private static final long serialVersionUID = -9036545818401656956L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			finish = (index >= quizs.length);
			if(finish) {
				u.output("为了让你更容易被发现，请用一句话介绍自己：");
			} else {
				u.output(quizs[index]);
			}
			
			if(index > 0) {
				u.content().account().put(String.join(Const.delimiter, Const.Version.V1, openid(), "Single", "Question", time(), ""+index), t);
			}
			
			index++;
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			if(finish()) {
				quiz.clear();
				return finished;
			}
			return this;
		}

		@Override
		public String name() {
			return "我要提问";
		}

		@Override
		public boolean finish() {
			return finish;
		}
	};
	
	State<Context<CorpusService>> answerBoy = new State<Context<CorpusService>>() {
		
		State<Context<CorpusService>> finished = new State<Context<CorpusService>>() {

			/**
			 * 
			 */
			private static final long serialVersionUID = -8031796296634830830L;

			@Override
			public void accept(String t, Context<CorpusService> u) {
				u.content().account().put(String.join(Const.delimiter, Const.Version.V1, "Info", "Self", "Introduce", openid()),  t);
				u.content().account().put(String.join(Const.delimiter, Const.Version.V1, openid(), "Self", "Introduce"),  t);
				u.content().data().put(String.join(Const.delimiter, Const.Version.V1, "Info", "Self", "Introduce", openid(), time()),  t);
				u.output("太好了，未来男朋友正在寻找你！你可以多提一些问题，或者多回答一些问题。");
			}

			@Override
			public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
				return init;
			}

			@Override
			public String name() {
				return "完成";
			}

			@Override
			public boolean finish() {
				return true;
			}
			
		};
		
		State<Context<CorpusService>> reply = new State<Context<CorpusService>>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 637372941813127676L;
			List<KeyValue> quizs = null;
			List<KeyValue> replies = new ArrayList<>();
			KeyValue q = null;
			Iterator<KeyValue> itr = null;
			@Override
			public void accept(String t, Context<CorpusService> u) {
				if(quizs == null || quizs.isEmpty()) {
					quizs = quiz(u.content(), current.key());
					itr = quizs.iterator();
				}
				if(q != null) {
					KeyValue rpl = new KeyValue(q.value(), t);
					replies.add(rpl);
				}
				if(itr.hasNext()) {
					q = itr.next();
					u.output(q.value());
				} else {
					StringBuffer sb = new StringBuffer();
					sb.append("她回答了你的问题，你看看怎么样？\n");
					for(KeyValue kv : replies) {
						sb.append("问：" + kv.key()).append("\n");
						sb.append("答：" + kv.value()).append("\n\n");
					}
					u.content().atUser(current.key(), sb.toString());
					u.output("未来男朋友觉得OK的话，会和你联系哦");
				}
			}

			@Override
			public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
				if(finish()) {
					quizs.clear();
					return answerBoy;
				}
				return this;
			}

			@Override
			public String name() {
				return "回复问题";
			}

			@Override
			public boolean finish() {
				return !itr.hasNext();
			}
			
		};

		/**
		 * 
		 */
		private static final long serialVersionUID = -8031796296634830830L;
		List<KeyValue> singles = new ArrayList<>();
		Iterator<KeyValue> iterator = null;
		KeyValue current = null;
		boolean finish = false;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			if(singles.isEmpty()) {
				singles = singles(u.content(), "BoySingle");
				iterator = singles.iterator();
				u.output("接下来是一些未来男朋友的自我介绍，如果觉得满意就输入：可以");
			}
			if(iterator.hasNext()) {
				current = iterator.next();
				u.output("他的自我介绍：");
				u.output(current.value());
			} else {
				u.output("先歇一会儿呗，为了让你更容易被发现，请用一句话介绍自己：");
			}
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			if(finish()) {
				singles.clear();
				return finished;
			}
			finish = !iterator.hasNext();
			if(u.yes(t, "可以")) {
				return reply;
			}
			return this;
		}

		@Override
		public String name() {
			return "我要回答";
		}

		@Override
		public boolean finish() {
			return finish;
		}
		
	};
	
	
	public List<KeyValue> quiz(CorpusService service, String openid) {
		String start = String.join(Const.delimiter, Const.Version.V1, openid, "Single", "Question");
		List<KeyValue> kvs = new ArrayList<>();
		service.account().page(start, start, null, 4, (k,v) ->{
			kvs.add(new KeyValue(k, v));
		});
		return kvs;
	}
	
	private String lastKey = null;
	public List<KeyValue> singles(CorpusService service, String tag) {
		String head = String.join(Const.delimiter, Const.Version.V1, "Tag", tag);
		String start = head;
		if(lastKey != null) {
			start = lastKey;
		}
		List<String> openids = new ArrayList<>();
		service.account().page(head, start, null, 3, (k,v) ->{
			openids.add(v);
			lastKey = k;
		});
		List<KeyValue> kvs = new ArrayList<>();
		openids.forEach(userid->{
			String intro = service.account().get(String.join(Const.delimiter, Const.Version.V1, userid, "Self", "Introduce"));
			kvs.add(new KeyValue(userid, intro));
			service.atUser(userid, "有单身正在阅读你的自我介绍哦，说不定还会回答你的提问呢～");
		});
		return kvs;
	}

	
	State<Context<CorpusService>> answerGirl = new State<Context<CorpusService>>() {
		State<Context<CorpusService>> finished = new State<Context<CorpusService>>() {

			/**
			 * 
			 */
			private static final long serialVersionUID = -8031796296634830830L;

			@Override
			public void accept(String t, Context<CorpusService> u) {
				u.content().account().put(String.join(Const.delimiter, Const.Version.V1, "Info", "Self", "Introduce", openid()),  t);
				u.content().account().put(String.join(Const.delimiter, Const.Version.V1, openid(), "Self", "Introduce"),  t);
				u.content().data().put(String.join(Const.delimiter, Const.Version.V1, "Info", "Self", "Introduce", openid(), time()),  t);
				u.output("太好了，未来女朋友正在寻找你！你可以多提一些问题，或者多回答一些问题。");
			}

			@Override
			public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
				return init;
			}

			@Override
			public String name() {
				return "完成";
			}

			@Override
			public boolean finish() {
				return true;
			}
			
		};
		
		State<Context<CorpusService>> reply = new State<Context<CorpusService>>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = -2626648875391367360L;

			List<KeyValue> quizs = null;
			List<KeyValue> replies = new ArrayList<>();
			KeyValue q = null;
			Iterator<KeyValue> itr = null;
			@Override
			public void accept(String t, Context<CorpusService> u) {
				if(quizs == null || quizs.isEmpty()) {
					quizs = quiz(u.content(), current.key());
					itr = quizs.iterator();
				}
				if(q != null) {
					KeyValue rpl = new KeyValue(q.value(), t);
					replies.add(rpl);
				}
				if(itr.hasNext()) {
					q = itr.next();
					u.output(q.value());
				} else {
					StringBuffer sb = new StringBuffer();
					sb.append("他回答了你的问题，你看看怎么样？\n");
					for(KeyValue kv : replies) {
						sb.append("问：" + kv.key()).append("\n");
						sb.append("答：" + kv.value()).append("\n\n");
					}
					u.content().atUser(current.key(), sb.toString());
					u.output("未来女朋友觉得OK的话，会和你联系哦");
				}
			}

			@Override
			public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
				if(finish()) {
					quizs.clear();
					return answerGirl;
				}
				return this;
			}

			@Override
			public String name() {
				return "回复问题";
			}

			@Override
			public boolean finish() {
				return !itr.hasNext();
			}
			
		};

		/**
		 * 
		 */
		private static final long serialVersionUID = -8031796296634830830L;
		List<KeyValue> singles = new ArrayList<>();
		Iterator<KeyValue> iterator = null;
		KeyValue current = null;
		boolean finish = false;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			if(singles.isEmpty()) {
				singles = singles(u.content(), "GirlSingle");
				iterator = singles.iterator();
				u.output("接下来是一些未来女朋友的自我介绍，如果觉得满意就输入：可以");
			}
			if(iterator.hasNext()) {
				current = iterator.next();
				u.output("她的自我介绍：");
				u.output(current.value());
			} else {
				u.output("先歇一会儿呗，为了让你更容易被发现，请用一句话介绍自己：");
			}
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			if(finish()) {
				singles.clear();
				return finished;
			}
			finish = !iterator.hasNext();
			if(u.yes(t, "可以")) {
				return reply;
			}
			return this;
		}

		@Override
		public String name() {
			return "我要回答";
		}
		
		@Override
		public boolean finish() {
			return finish;
		}
		
	};
	
	State<Context<CorpusService>> girlfriend = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = -9036545818401656956L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			u.content().account().put(String.join(Const.delimiter, Const.Version.V1, openid(), "Tag", "BoySingle"), openid());
			u.content().account().put(String.join(Const.delimiter, Const.Version.V1, "Tag", "BoySingle", openid()), openid());
			u.output("你的未来女朋友也正在寻找你，你可以问她几个问题或者回答她的问题，请输入：\n我要提问\n我要回答");
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			if(question.name().equals(t)) return question;
			if(answerGirl.name().equals(t)) return answerGirl;
			return this;
		}

		@Override
		public String name() {
			return "我要女朋友";
		}

		@Override
		public boolean finish() {
			return false;
		}
	};
	
	
	State<Context<CorpusService>> boyfriend = new State<Context<CorpusService>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = -9036545818401656956L;

		@Override
		public void accept(String t, Context<CorpusService> u) {
			u.content().account().put(String.join(Const.delimiter, Const.Version.V1, openid(), "Tag", "GirlSingle"), openid());
			u.content().account().put(String.join(Const.delimiter, Const.Version.V1, "Tag", "GirlSingle", openid()), openid());
			u.output("你的未来男朋友也正在寻找你，你可以问他一些问题或者回答他的问题，请输入：\n我要提问\n我要回答");
		}

		@Override
		public State<Context<CorpusService>> apply(String t, Context<CorpusService> u) {
			if(question.name().equals(t)) return question;
			if(answerBoy.name().equals(t)) return answerBoy;
			return this;
		}

		@Override
		public String name() {
			return "我要男朋友";
		}

		@Override
		public boolean finish() {
			return false;
		}
	};
	
	public SingleContext(String wxid, String openid, CorpusService ctx) {
		super(wxid, openid, ctx);
		this.state(single);
	}

}
