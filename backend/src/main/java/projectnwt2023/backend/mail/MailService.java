package projectnwt2023.backend.mail;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import projectnwt2023.backend.appUser.AppUser;
import projectnwt2023.backend.helper.Constants;
import projectnwt2023.backend.property.Property;

import java.io.IOException;

@Service
public class MailService {
    private static final Logger logger = LoggerFactory.getLogger(MailService.class);

    public String sendTextEmail(String emailTo, String subject, String textContent) throws IOException {
        Email from = new Email(Constants.fromEmail);
        Email to = new Email(emailTo);
        Content content = new Content("text/plain", textContent);
        Mail mail = new Mail(from, subject, to, content);

        SendGrid sg = new SendGrid(Constants.sendgridKey);
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);
            logger.info(response.getBody());
            return response.getBody();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            return null;
        }
    }

    public String sendApprovalEmail(Property property) throws IOException {

        Mail mail = new Mail();
        mail.setFrom(new Email(Constants.fromEmail));
        mail.setTemplateId("d-086477fcd9144491b6eb015765d305d1");

        Personalization personalization = new Personalization();
        personalization.addTo(new Email(property.getPropertyOwner().getEmail()));
        personalization.addDynamicTemplateData("name", property.getPropertyOwner().getName());
        personalization.addDynamicTemplateData("lastName", property.getPropertyOwner().getLastName());
        personalization.addDynamicTemplateData("propertyType", property.getPropertyType().toString().toLowerCase());
        personalization.addDynamicTemplateData("address", property.getAddress());
        personalization.addDynamicTemplateData("city", property.getCity().getName());
        mail.addPersonalization(personalization);

        SendGrid sg = new SendGrid(Constants.sendgridKey);
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);
            logger.info(response.getBody());
            return response.getBody();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            return null;
        }
    }

    public String sendRejectionEmail(Property property, String reason) throws IOException {

        Mail mail = new Mail();
        mail.setFrom(new Email(Constants.fromEmail));
        mail.setTemplateId("d-fbcae4360c494b2994a8ac2b989d24ef");

        Personalization personalization = new Personalization();
        personalization.addTo(new Email(property.getPropertyOwner().getEmail()));
        personalization.addDynamicTemplateData("name", property.getPropertyOwner().getName());
        personalization.addDynamicTemplateData("lastName", property.getPropertyOwner().getLastName());
        personalization.addDynamicTemplateData("propertyType", property.getPropertyType().toString().toLowerCase());
        personalization.addDynamicTemplateData("address", property.getAddress());
        personalization.addDynamicTemplateData("city", property.getCity().getName());
        personalization.addDynamicTemplateData("reason", reason);
        mail.addPersonalization(personalization);

        SendGrid sg = new SendGrid(Constants.sendgridKey);
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);
            logger.info(response.getBody());
            return response.getBody();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            return null;
        }
    }


    public String sendActivationEmail(AppUser appUser, String token) throws IOException {

        Mail mail = new Mail();
        mail.setFrom(new Email(Constants.fromEmail));
        mail.setTemplateId("d-2d93501e4d4e4954ad2f2eb850d16f45");

        Personalization personalization = new Personalization();
        personalization.addTo(new Email(appUser.getEmail()));
        personalization.addDynamicTemplateData("name", appUser.getName());
        personalization.addDynamicTemplateData("token", token);
        mail.addPersonalization(personalization);

        SendGrid sg = new SendGrid(Constants.sendgridKey);
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);
            logger.info(response.getBody());
            return response.getBody();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            return null;
        }
    }





    public String sendTextEmailMilos(String toEmail, String sub, String text) throws IOException {
        Email from = new Email(Constants.fromEmail);
        String subject = sub;
        Email to = new Email(toEmail);
        Content content = new Content("text/plain", text);
        Mail mail = new Mail(from, subject, to, content);

        SendGrid sg = new SendGrid(Constants.sendgridKey);
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);
            logger.info(response.getBody());
            return response.getBody();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            return null;
        }
    }
}