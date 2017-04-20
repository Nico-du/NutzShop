package cn.wizzer.app.web.modules.controllers.platform.goods;

import cn.wizzer.app.shop.modules.models.Shop_goods_spec;
import cn.wizzer.app.shop.modules.models.Shop_goods_type_spec;
import cn.wizzer.app.shop.modules.services.ShopGoodsSpecService;
import cn.wizzer.app.shop.modules.services.ShopGoodsSpecValuesService;
import cn.wizzer.app.shop.modules.services.ShopGoodsTypeService;
import cn.wizzer.app.shop.modules.services.ShopGoodsTypeSpecService;
import cn.wizzer.app.web.commons.slog.annotation.SLog;
import cn.wizzer.framework.base.Result;
import cn.wizzer.framework.page.datatable.DataTableColumn;
import cn.wizzer.framework.page.datatable.DataTableOrder;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.nutz.dao.Cnd;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Strings;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@IocBean
@At("/platform/shop/goods/spec")
public class ShopGoodsSpecController {
    private static final Log log = Logs.get();
    @Inject
    private ShopGoodsSpecService shopGoodsSpecService;
    @Inject
    private ShopGoodsSpecValuesService shopGoodsSpecValuesService;
    @Inject
    private ShopGoodsTypeSpecService shopGoodsTypeSpecService;
    @Inject
    private ShopGoodsTypeService shopGoodsTypeService;

    @At("/image")
    @Ok("beetl:/platform/shop/goods/spec/image.html")
    @RequiresAuthentication
    public void index(@Param("w") int w, @Param("h") int h, HttpServletRequest req) {
        req.setAttribute("w", w);
        req.setAttribute("h", h);
    }

    @At("")
    @Ok("beetl:/platform/shop/goods/spec/index.html")
    @RequiresAuthentication
    public void index() {

    }

    @At
    @Ok("json:full")
    @RequiresAuthentication
    public Object data(@Param("length") int length, @Param("start") int start, @Param("draw") int draw, @Param("::order") List<DataTableOrder> order, @Param("::columns") List<DataTableColumn> columns) {
        Cnd cnd = Cnd.NEW();
        return shopGoodsSpecService.data(length, start, draw, order, columns, cnd, null);
    }

    @At
    @Ok("beetl:/platform/shop/goods/spec/add.html")
    @RequiresAuthentication
    public void add() {

    }

    @At
    @Ok("json")
    @RequiresPermissions("shop.goods.conf.spec.add")
    @SLog(tag = "新建商品规格", msg = "规格名称:${args[0].name}")
    public Object addDo(@Param("..") Shop_goods_spec shopGoodsSpec, @Param("spec_value") String[] spec_value, @Param("spec_picurl") String[] spec_picurl, HttpServletRequest req) {
        try {
            shopGoodsSpecService.add(shopGoodsSpec, spec_value, spec_picurl);
            return Result.success("system.success");
        } catch (Exception e) {
            return Result.error("system.error");
        }
    }

    @At("/edit/?")
    @Ok("beetl:/platform/shop/goods/spec/edit.html")
    @RequiresAuthentication
    public Object edit(String id) {
        return shopGoodsSpecService.fetchLinks(shopGoodsSpecService.fetch(id), "specValues", Cnd.orderBy().asc("location"));
    }

    @At
    @Ok("json")
    @RequiresPermissions("shop.goods.conf.spec.edit")
    @SLog(tag = "修改商品规格", msg = "规格名称:${args[0].name}")
    public Object editDo(@Param("..") Shop_goods_spec shopGoodsSpec, @Param("spec_value") String[] spec_value, @Param("spec_picurl") String[] spec_picurl, @Param("spec_value_id") String[] spec_value_id, HttpServletRequest req) {
        try {
            shopGoodsSpecService.update(shopGoodsSpec, spec_value, spec_picurl, spec_value_id, Strings.sNull(req.getAttribute("uid")));
            return Result.success("system.success");
        } catch (Exception e) {
            return Result.error("system.error");
        }
    }


    @At({"/delete/?"})
    @Ok("json")
    @RequiresPermissions("shop.goods.conf.spec.delete")
    @SLog(tag = "删除商品规格", msg = "ID:${args[2].getAttribute('id')}")
    public Object delete(String id, HttpServletRequest req) {
        try {
            List<Shop_goods_type_spec> templist = shopGoodsTypeSpecService.query(Cnd.where("specId", "=", id));
            if (templist.size() > 0) {
                StringBuilder errMsg = new StringBuilder();
                for (Shop_goods_type_spec goodsSpecTemp : templist) {
                    errMsg.append(" " + shopGoodsTypeService.fetch(goodsSpecTemp.getTypeId()).getName());
                }
                return Result.error("在" + errMsg.toString() + " 类型中已使用，不允许删除");
            }
            shopGoodsSpecService.deleteSpec(id);
            req.setAttribute("id", id);
            return Result.success("system.success");
        } catch (Exception e) {
            return Result.error("system.error");
        }
    }

}
