package com.smartstream.sort.api.core.proxy;

import com.smartstream.sort.api.core.entity.Entity;
import com.smartstream.sort.api.core.entity.ProxyController;
import com.smartstream.sort.api.core.entity.RefNode;

public class RefNodeProxyHelper {

    private Object refProxy;
    public final RefNode refNode;

    public RefNodeProxyHelper(RefNode refNode) {
        this.refNode = refNode;
    }

    @Override
    public String toString() {
        Entity ref = refNode.getReference();
        if (ref == null) {
            refProxy = null;
        }
        else if (refProxy == null || ((ProxyController) refProxy).getEntity() != ref) {
            refProxy = ref.getEntityContext().getProxy(ref);
        }
        return String.valueOf(ref);
    }

}