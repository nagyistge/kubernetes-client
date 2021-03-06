/**
 * Copyright (C) 2015 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.fabric8.kubernetes.client.mock;

import org.junit.Test;

import java.io.InputStream;

import io.fabric8.kubernetes.api.model.KubernetesList;
import io.fabric8.kubernetes.api.model.KubernetesListBuilder;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.ReplicationController;
import io.fabric8.kubernetes.api.model.ReplicationControllerBuilder;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class KubernetesListTest extends KubernetesMockServerTestBase {

  Pod pod1 = new PodBuilder().withNewMetadata().withName("pod1").withNamespace("test").and().build();
  Service service1 = new ServiceBuilder().withNewMetadata().withName("service1").withNamespace("test").and().build();
  ReplicationController replicationController1 = new ReplicationControllerBuilder().withNewMetadata().withName("repl1").withNamespace("test").and().build();

  KubernetesList list = new KubernetesListBuilder().withItems(pod1, service1, replicationController1).build();


  @Test
  public void testCreate() {
    expect().withPath("/api/v1/namespaces/test/pods").andReturn(201, pod1).once();
    expect().withPath("/api/v1/namespaces/test/services").andReturn(201, service1).once();
    expect().withPath("/api/v1/namespaces/test/replicationcontrollers").andReturn(201, replicationController1).once();

    KubernetesClient client = getClient();
    KubernetesList result = client.lists().inNamespace("test").create(list);

    assertNotNull(result);
    assertEquals(3, result.getItems().size());


    assertTrue(result.getItems().contains(pod1));
    assertTrue(result.getItems().contains(service1));
    assertTrue(result.getItems().contains(replicationController1));
  }

  @Test
  public void testLoadAndCreate() {
    expect().withPath("/api/v1/namespaces/test/replicationcontrollers").andReturn(201, replicationController1).times(2);

    KubernetesClient client = getClient();
    InputStream is = KubernetesListTest.class.getResourceAsStream("/test-rclist.json");
    KubernetesList result = client.inNamespace("test").lists().load(is).create();

    assertNotNull(result);
    assertEquals(2, result.getItems().size());
  }

  @Test
  public void testDelete() {
    expect().withPath("/api/v1/namespaces/test/pods/pod1").andReturn(200, pod1).always();
    expect().withPath("/api/v1/namespaces/test/services/service1").andReturn(200, service1).always();
    expect().withPath("/api/v1/namespaces/test/replicationcontrollers/repl1").andReturn(200, replicationController1).always();

    KubernetesClient client = getClient();
    Boolean result = client.lists().delete(list);

    assertTrue(result);
  }

  @Test(expected = KubernetesClientException.class)
  public void testDeleteWithMismatch() {
    KubernetesClient client = getClient();
    Boolean result = client.lists().inNamespace("test1").delete(list);

    assertTrue(result);
  }
}
