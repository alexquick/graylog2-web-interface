class ApplicationController < ActionController::Base
  include AuthenticatedSystem

  protect_from_forgery
  helper Authorization::AuthorizationHelper
  @@api_auth_only = []
  before_filter :login_required, :clear_terms_cache

  rescue_from "Mongo::ConnectionFailure" do
      render_custom_error_page("mongo_connectionfailure") and return
  end

  rescue_from "RestClient::ResourceNotFound" do
      render_custom_error_page("elasticsearch_noindex") and return
  end

  rescue_from "Errno::ECONNREFUSED" do
      render_custom_error_page("elasticsearch_noconnection") and return
  end

  helper_method :has_users
  def has_users
    return false if User.count == 0
    return true
  end

  # TODO remove, replace with time_to_formatted_s
  helper_method :gl_date
  def gl_date(date)
    date = date.to_s
    return String.new if date == nil or date.length == 0
    tmp = DateTime.parse(date)
    return tmp.strftime(::Configuration.date_format)
  end

  def self.api_auth(*actions)
    @@api_auth_only = actions
  end

  def api_auth_only(action)
    return @@api_auth_only.include?(action.to_sym)
  end

  private

  def logged_in?
    begin
      return true if current_user
    end
    return false
  end

  def api_login
    logger.info("using api login")
    key = params[:api_key]
    return nil unless key
    return User.find_by_key(key)
  end

  def login_required
    if  request.format.json? then
      return true if logged_in? and not api_auth_only(action_name)
      @current_user = api_login() || false
      if !logged_in?
        render :json => {"error", "unauthorized"}, :status=>401
        return false
      end
    end
    if !logged_in?
      store_location
      redirect_to login_path
    else
      return true
    end
    return false
  end

  def not_found
    render :file  => "#{Rails.root.to_s}/public/404.html", :status => 404, :layout => false
  end

  def clear_terms_cache
    FilteredTerm.expire_cache
  end

  def render_custom_error_page(tpl)
    render :file => "#{Rails.root.to_s}/public/#{tpl}.html", :status => 500, :layout => false
    return
  end
end
