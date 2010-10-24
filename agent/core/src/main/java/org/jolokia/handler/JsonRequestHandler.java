package org.jolokia.handler;

import org.jolokia.JmxRequest;
import org.jolokia.config.Restrictor;

import javax.management.*;
import java.io.IOException;
import java.util.Set;

/*
 *  Copyright 2009-2010 Roland Huss
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */


/**
 * @author roland
 * @since Jun 12, 2009
 */
public abstract class JsonRequestHandler {

    // Restrictor for restricting operations

    private final Restrictor restrictor;

    protected JsonRequestHandler(Restrictor pRestrictor) {
        restrictor = pRestrictor;
    }


    /**
     * The type of request which can be served by this handler
     * @return the request typ of this handler
     */
    public abstract JmxRequest.Type getType();

    /**
     * Override this if you want all servers as list in the argument, e.g.
     * to query each server on your own. By default, dispatching of the servers
     * are done for you
     *
     * @param pRequest request to decide on whether to handle all request at once
     * @return whether you want to have
     * {@link #doHandleRequest(javax.management.MBeanServerConnection, org.jolokia.JmxRequest)}
     * (<code>false</code>) or
     * {@link #doHandleRequest(java.util.Set, org.jolokia.JmxRequest)} (<code>true</code>) called.
     */
    public boolean handleAllServersAtOnce(JmxRequest pRequest) {
        return false;
    }

    /**
     * Handle a request for a single server and throw an
     * {@link javax.management.InstanceNotFoundException}
     * if the request cannot be handle by the provided server.
     * Does a check for restrictions as well
     *
     * @param pServer server to try
     * @param pRequest request to process
     * @return the object result from the request
     *
     * @throws InstanceNotFoundException if the provided server cant handle the request
     * @throws AttributeNotFoundException
     * @throws ReflectionException
     * @throws MBeanException
     * @throws java.io.IOException
     */
    public Object handleRequest(MBeanServerConnection pServer,JmxRequest pRequest)
            throws InstanceNotFoundException, AttributeNotFoundException, ReflectionException, MBeanException, IOException {
        checkForRestriction(pRequest);
        checkHttpMethod(pRequest);
        return doHandleRequest(pServer, pRequest);
    }

    /**
     * Check whether there is a restriction on the type to apply. This method should be overwritten
     * by specific handlers if they support a more sophisticated check than only for the type
     *
     * @param pRequest request to check
     */
    protected void checkForRestriction(JmxRequest pRequest) {
        if (!restrictor.isTypeAllowed(getType())) {
            throw new SecurityException("Command type " +
                    getType() + " not allowed due to policy used");
        }
    }

    /**
     * Check whether the HTTP method with which the request was sent is allowed according to the policy
     * installed
     *
     * @param pRequest request to check
     */
    private void checkHttpMethod(JmxRequest pRequest) {
        if (!restrictor.isHttpMethodAllowed(pRequest.getHttpMethod())) {
            throw new SecurityException("HTTP method " + pRequest.getHttpMethod().getMethod() +
                    " is not allowed according to the installed security policy");
        }
    }

    /**
     * Abstract method to be subclassed by a concrete handler for performing the
     * request.
     *
     * @param server server to try
     * @param request request to process
     * @return the object result from the request
     *
     * @throws InstanceNotFoundException
     * @throws AttributeNotFoundException
     * @throws ReflectionException
     * @throws MBeanException
     */
    protected abstract Object doHandleRequest(MBeanServerConnection server,JmxRequest request)
            throws InstanceNotFoundException, AttributeNotFoundException, ReflectionException, MBeanException, IOException;

    /**
     * Override this if you want to have all servers at once for processing the request
     * (like need for merging info as for a <code>list</code> command). This method
     * is only called when {@link #handleAllServersAtOnce(JmxRequest)} returns <code>true</code>
     *
     * @param servers all MBeans servers detected
     * @param request request to process
     * @return the object found
     */
    public Object handleRequest(Set<MBeanServerConnection> servers, JmxRequest request)
            throws ReflectionException, InstanceNotFoundException, MBeanException, AttributeNotFoundException, IOException {
        checkForRestriction(request);
        return doHandleRequest(servers,request);
    }

    /**
     * Default implementation fo handling a request for multiple servers at once. A subclass, which returns,
     * <code>true</code> on {@link #handleAllServersAtOnce(JmxRequest)}, needs to override this method.
     *
     * @param servers all MBean servers found in this JVM
     * @param request the original request
     * @return the result of the the request.
     */
    public Object doHandleRequest(Set<MBeanServerConnection> servers, JmxRequest request)
                throws InstanceNotFoundException, AttributeNotFoundException, ReflectionException, MBeanException, IOException {
        return null;
    }

    /**
     * Use the path for the return value by default
     *
     * @return true
     */
    public boolean useReturnValueWithPath() {
        return true;
    }

    /**
     * Get the restrictor which is currently active
     *
     * @return restrictor
     */
    protected Restrictor getRestrictor() {
        return restrictor;
    }

}
