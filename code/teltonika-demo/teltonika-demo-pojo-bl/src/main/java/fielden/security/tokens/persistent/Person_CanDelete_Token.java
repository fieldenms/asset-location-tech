package fielden.security.tokens.persistent;

import static java.lang.String.format;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitleAndDesc;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getTitleAndDesc;

import fielden.security.tokens.UsersAndPersonnelModuleToken;
import fielden.personnel.Person;
import ua.com.fielden.platform.security.tokens.Template;

/**
 * A security token for entity {@link Person} to guard DELETE. 
 * 
 * @author Generated
 *
 */
public class Person_CanDelete_Token extends UsersAndPersonnelModuleToken {
    private final static String ENTITY_TITLE = getEntityTitleAndDesc(Person.class).getKey();
    public final static String TITLE = format(Template.DELETE.forTitle(), ENTITY_TITLE);
    public final static String DESC = format(Template.DELETE.forDesc(), ENTITY_TITLE);
}