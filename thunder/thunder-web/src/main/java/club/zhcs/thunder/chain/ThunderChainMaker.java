package club.zhcs.thunder.chain;

import java.util.ArrayList;
import java.util.List;

import org.apache.shiro.authz.aop.AuthorizingAnnotationMethodInterceptor;
import org.nutz.integration.shiro.NutShiroProcessor;
import org.nutz.lang.ContinueLoop;
import org.nutz.lang.Each;
import org.nutz.lang.ExitLoop;
import org.nutz.lang.Lang;
import org.nutz.lang.LoopException;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.ActionChain;
import org.nutz.mvc.ActionInfo;
import org.nutz.mvc.NutConfig;
import org.nutz.mvc.Processor;
import org.nutz.mvc.impl.NutActionChain;
import org.nutz.mvc.impl.processor.ActionFiltersProcessor;

import club.zhcs.thunder.ext.shiro.anno.ThunderRequiresPermissions;
import club.zhcs.thunder.ext.shiro.anno.ThunderRequiresRoles;
import club.zhcs.thunder.ext.shiro.aop.ThunderPermissionAnnotationMethodInterceptor;
import club.zhcs.thunder.ext.shiro.aop.ThunderRoleAnnotationMethodInterceptor;
import club.zhcs.titans.nutz.chain.KerboresActionChainMaker;
import club.zhcs.titans.nutz.processor.KerboresFailProcessor;

/**
 * 
 * @author Kerbores(kerbores@gmail.com)
 *
 * @project thunder-web
 *
 * @file ThunderChainMaker.java
 *
 * @description
 *
 * @copyright 内部代码,禁止转发
 *
 * @time 2016年5月16日 下午11:22:43
 *
 */
public class ThunderChainMaker extends KerboresActionChainMaker {

	private Log log = Logs.get();

	@Override
	public ActionChain eval(final NutConfig config, final ActionInfo ai) {
		List<Processor> list = normalList();

		List<AuthorizingAnnotationMethodInterceptor> interceptors = new
				ArrayList<AuthorizingAnnotationMethodInterceptor>();

		interceptors.add(new ThunderPermissionAnnotationMethodInterceptor());
		interceptors.add(new ThunderRoleAnnotationMethodInterceptor());

		addBefore(list, ActionFiltersProcessor.class, new
				NutShiroProcessor(interceptors, ThunderRequiresPermissions.class,
						ThunderRequiresRoles.class));

		addBefore(list, ActionFiltersProcessor.class, new
				WxUserInjectProcessor());
		addBefore(list, ActionFiltersProcessor.class, new
				WxJsSdkConfigProcessor());

		Processor error = new KerboresFailProcessor();
		Lang.each(list, new Each<Processor>() {

			@Override
			public void invoke(int paramInt1, Processor processor, int paramInt2) throws ExitLoop, ContinueLoop, LoopException {
				try {
					processor.init(config, ai);
				} catch (Throwable e) {
					log.error(e);
				}
			}
		});
		try {
			error.init(config, ai);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return new NutActionChain(list, error, ai);
	}
}
