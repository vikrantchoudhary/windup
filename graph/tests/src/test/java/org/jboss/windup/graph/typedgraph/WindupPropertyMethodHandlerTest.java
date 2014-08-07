package org.jboss.windup.graph.typedgraph;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.Dependencies;
import org.jboss.forge.arquillian.archive.ForgeArchive;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.service.GraphService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.thinkaurelius.titan.core.attribute.Text;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@RunWith(Arquillian.class)
public class WindupPropertyMethodHandlerTest
{

    @Deployment
    @Dependencies({
                @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
                @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static ForgeArchive getDeployment()
    {
        ForgeArchive archive = ShrinkWrap.create(ForgeArchive.class)
                    .addBeansXML()
                    .addClasses(FooModel.class, FooSubModel.class)
                    .addAsAddonDependencies(
                                AddonDependencyEntry.create("org.jboss.windup.graph:windup-graph"),
                                AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi")
                    );
        return archive;
    }

    @Inject
    private GraphContext context;

    @Test
    public void testInMemoryFrame() throws Exception
    {
        Assert.assertNotNull(context);

        GraphService<FooModel> fooModelService = context.getService(FooModel.class);

        FooModel inMemoryModel = fooModelService.create();
        inMemoryModel.setProp1("prop1").setProp2("prop2").setProp3("prop3");

        Iterable<Vertex> vertices = context.getFramed().query()
                    .has("type", Text.CONTAINS, FooModel.class.getAnnotation(TypeValue.class).value())
                    .vertices();

        int numberFound = 0;
        for (Vertex v : vertices)
        {
            numberFound++;
            FooModel framed = (FooModel) context.getFramed().frame(v, WindupVertexFrame.class);

            Assert.assertTrue(framed instanceof FooModel);
            Assert.assertEquals("prop1", framed.getProp1());
            Assert.assertEquals("prop2", framed.getProp2());
            Assert.assertEquals("prop3", framed.getProp3());
        }
        Assert.assertEquals(1, numberFound);
    }
}